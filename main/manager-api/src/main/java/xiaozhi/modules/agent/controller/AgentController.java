package xiaozhi.modules.agent.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.agent.dto.AgentChatHistoryDTO;
import xiaozhi.modules.agent.dto.AgentChatSessionDTO;
import xiaozhi.modules.agent.dto.AgentCreateDTO;
import xiaozhi.modules.agent.dto.AgentDTO;
import xiaozhi.modules.agent.dto.AgentMemoryDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import xiaozhi.modules.agent.dto.AgentTagDTO;
import xiaozhi.modules.agent.entity.AgentTagEntity;
import xiaozhi.modules.agent.service.AgentTagService;
import xiaozhi.modules.agent.service.AgentChatAudioService;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentChatSummaryService;
import xiaozhi.modules.agent.service.AgentContextProviderService;
import xiaozhi.modules.agent.service.AgentPluginMappingService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.service.AgentTemplateService;
import xiaozhi.modules.agent.vo.AgentChatHistoryUserVO;
import xiaozhi.modules.agent.vo.AgentInfoVO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.user.SecurityUser;

@Tag(name = "Agent management")
@AllArgsConstructor
@RestController
@RequestMapping("/agent")
public class AgentController {
    private final AgentService agentService;
    private final AgentTemplateService agentTemplateService;
    private final DeviceService deviceService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentChatAudioService agentChatAudioService;
    private final AgentPluginMappingService agentPluginMappingService;
    private final AgentContextProviderService agentContextProviderService;
    private final AgentChatSummaryService agentChatSummaryService;
    private final RedisUtils redisUtils;
    private final AgentTagService agentTagService;

    @GetMapping("/list")
    @Operation(summary = "getuserAgent list")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentDTO>> getUserAgents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", defaultValue = "name") String searchType) {
        UserDetail user = SecurityUser.getUser();

        // directlycallwholemergeafter getUserAgentsmethod，noneedagainareasearchandordinaryquery
        List<AgentDTO> agents = agentService.getUserAgents(user.getId(), keyword, searchType);
        return new Result<List<AgentDTO>>().ok(agents);
    }

    @GetMapping("/all")
    @Operation(summary = "Agent list（administrator）")
    @RequiresPermissions("sys:role:superAdmin")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "currentpage number，from1start", required = true),
            @Parameter(name = Constant.LIMIT, description = "per pagerecordnumber", required = true),
    })
    public Result<PageData<AgentEntity>> adminAgentList(
            @Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<AgentEntity> page = agentService.adminAgentList(params);
        return new Result<PageData<AgentEntity>>().ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "getagentdetails")
    @RequiresPermissions("sys:role:normal")
    public Result<AgentInfoVO> getAgentById(@PathVariable("id") String id) {
        AgentInfoVO agent = agentService.getAgentById(id);
        return ResultUtils.success(agent);
    }

    @PostMapping
    @Operation(summary = "createagent")
    @RequiresPermissions("sys:role:normal")
    public Result<String> save(@RequestBody @Valid AgentCreateDTO dto) {
        String agentId = agentService.createAgent(dto);
        return new Result<String>().ok(agentId);
    }

    @PutMapping("/saveMemory/{macAddress}")
    @Operation(summary = "according todeviceidupdateagent")
    public Result<Void> updateByDeviceId(@PathVariable String macAddress, @RequestBody @Valid AgentMemoryDTO dto) {
        DeviceEntity device = deviceService.getDeviceByMacAddress(macAddress);
        if (device == null) {
            return new Result<>();
        }
        AgentUpdateDTO agentUpdateDTO = new AgentUpdateDTO();
        agentUpdateDTO.setSummaryMemory(dto.getSummaryMemory());
        agentService.updateAgentById(device.getAgentId(), agentUpdateDTO);
        return new Result<>();
    }

    @PostMapping("/chat-summary/{sessionId}/save")
    @Operation(summary = "according toSession IDgenerateChat historysummaryandsave（asynchronousexecute）")
    public Result<Void> generateAndSaveChatSummary(@PathVariable String sessionId) {
        try {
            // asynchronousexecutesummarygeneratetask，immediatelyreturnsuccessresponse
            new Thread(() -> {
                try {
                    agentChatSummaryService.generateAndSaveChatSummary(sessionId);
                    System.out.println("asynchronousexecutesession " + sessionId + "  Chat historysummarycomplete");
                } catch (Exception e) {
                    System.err.println("asynchronousexecutesession " + sessionId + "  Chat historysummaryfailed: " + e.getMessage());
                }
            }).start();

            // immediatelyreturnsuccessresponse，not waitsummarygeneratecomplete
            return new Result<Void>().ok(null);
        } catch (Exception e) {
            return new Result<Void>().error("startasynchronoussummarygeneratetaskfailed: " + e.getMessage());
        }
    }

    @PostMapping("/chat-title/{sessionId}/generate")
    @Operation(summary = "according toSession IDgeneratechattitle")
    public Result<Void> generateAndSaveChatTitle(@PathVariable String sessionId) {
        agentChatSummaryService.generateAndSaveChatTitle(sessionId);
        return new Result<Void>().ok(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "updateagent")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> update(@PathVariable String id, @RequestBody @Valid AgentUpdateDTO dto) {
        agentService.updateAgentById(id, dto);
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "deleteagent")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> delete(@PathVariable String id) {
        // firstdeleteassociated device
        deviceService.deleteByAgentId(id);
        // deleteassociated Chat history
        agentChatHistoryService.deleteByAgentId(id, true, true);
        // deleteassociated plugin
        agentPluginMappingService.deleteByAgentId(id);
        // deleteassociated contextsourceconfiguration
        agentContextProviderService.deleteByAgentId(id);
        // againdeleteagent
        agentService.deleteById(id);
        return new Result<>();
    }

    @GetMapping("/template")
    @Operation(summary = "Agent templatetemplatelist")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentTemplateEntity>> templateList() {
        List<AgentTemplateEntity> list = agentTemplateService
                .list(new QueryWrapper<AgentTemplateEntity>().orderByAsc("sort"));
        return new Result<List<AgentTemplateEntity>>().ok(list);
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "getagentsessionlist")
    @RequiresPermissions("sys:role:normal")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "currentpage number，from1start", required = true),
            @Parameter(name = Constant.LIMIT, description = "per pagerecordnumber", required = true),
    })
    public Result<PageData<AgentChatSessionDTO>> getAgentSessions(
            @PathVariable("id") String id,
            @Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        params.put("agentId", id);
        PageData<AgentChatSessionDTO> page = agentChatHistoryService.getSessionListByAgentId(params);
        return new Result<PageData<AgentChatSessionDTO>>().ok(page);
    }

    @GetMapping("/{id}/chat-history/{sessionId}")
    @Operation(summary = "getagentChat history")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentChatHistoryDTO>> getAgentChatHistory(
            @PathVariable("id") String id,
            @PathVariable("sessionId") String sessionId) {
        // get currentuser
        UserDetail user = SecurityUser.getUser();

        // checkPermission
        if (!agentService.checkAgentPermission(id, user.getId())) {
            return new Result<List<AgentChatHistoryDTO>>().error("noPermissionviewthisagent Chat history");
        }

        // queryChat history
        List<AgentChatHistoryDTO> result = agentChatHistoryService.getChatHistoryBySessionId(id, sessionId);
        return new Result<List<AgentChatHistoryDTO>>().ok(result);
    }

    @GetMapping("/{id}/chat-history/user")
    @Operation(summary = "getagentChat history（user）")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentChatHistoryUserVO>> getRecentlyFiftyByAgentId(
            @PathVariable("id") String id) {
        // get currentuser
        UserDetail user = SecurityUser.getUser();

        // checkPermission
        if (!agentService.checkAgentPermission(id, user.getId())) {
            return new Result<List<AgentChatHistoryUserVO>>().error("noPermissionviewthisagent Chat history");
        }

        // queryChat history
        List<AgentChatHistoryUserVO> data = agentChatHistoryService.getRecentlyFiftyByAgentId(id);
        return new Result<List<AgentChatHistoryUserVO>>().ok(data);
    }

    @GetMapping("/{id}/chat-history/audio")
    @Operation(summary = "getaudiocontent")
    @RequiresPermissions("sys:role:normal")
    public Result<String> getContentByAudioId(
            @PathVariable("id") String id) {
        // queryChat history
        String data = agentChatHistoryService.getContentByAudioId(id);
        return new Result<String>().ok(data);
    }

    @PostMapping("/audio/{audioId}")
    @Operation(summary = "getaudiodownloadID")
    @RequiresPermissions("sys:role:normal")
    public Result<String> getAudioId(@PathVariable("audioId") String audioId) {
        byte[] audioData = agentChatAudioService.getAudio(audioId);
        if (audioData == null) {
            return new Result<String>().error("audiodoes not exist");
        }
        String uuid = UUID.randomUUID().toString();
        redisUtils.set(RedisKeys.getAgentAudioIdKey(uuid), audioId);
        return new Result<String>().ok(uuid);
    }

    @GetMapping("/play/{uuid}")
    @Operation(summary = "playaudio")
    public ResponseEntity<byte[]> playAudio(@PathVariable("uuid") String uuid) {

        String audioId = (String) redisUtils.get(RedisKeys.getAgentAudioIdKey(uuid));
        if (StringUtils.isBlank(audioId)) {
            return ResponseEntity.notFound().build();
        }

        byte[] audioData = agentChatAudioService.getAudio(audioId);
        if (audioData == null) {
            return ResponseEntity.notFound().build();
        }
        redisUtils.delete(RedisKeys.getAgentAudioIdKey(uuid));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"play.wav\"")
                .body(audioData);
    }

    @PostMapping("/tag")
    @Operation(summary = "createTag")
    @RequiresPermissions("sys:role:normal")
    public Result<AgentTagEntity> createTag(@RequestBody Map<String, String> params) {
        String tagName = params.get("tagName");
        if (StringUtils.isBlank(tagName)) {
            return new Result<AgentTagEntity>().error("Tagnamecannot be empty");
        }
        AgentTagEntity tag = agentTagService.saveTag(tagName);
        return new Result<AgentTagEntity>().ok(tag);
    }

    @GetMapping("/tag/list")
    @Operation(summary = "get allTaglist")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentTagDTO>> getAllTags() {
        List<AgentTagDTO> tags = agentTagService.getAllTags();
        return new Result<List<AgentTagDTO>>().ok(tags);
    }

    @DeleteMapping("/tag/{id}")
    @Operation(summary = "deleteTag")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> deleteTag(@PathVariable String id) {
        agentTagService.deleteTag(id);
        return new Result<Void>().ok(null);
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "getagent Tag")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentTagDTO>> getAgentTags(@PathVariable String id) {
        List<AgentTagDTO> tags = agentTagService.getTagsByAgentId(id);
        return new Result<List<AgentTagDTO>>().ok(tags);
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "saveagent Tag")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> saveAgentTags(@PathVariable String id, @RequestBody Map<String, Object> params) {
        List<String> tagIds = (List<String>) params.get("tagIds");
        List<String> tagNames = (List<String>) params.get("tagNames");
        agentTagService.saveAgentTags(id, tagIds, tagNames);
        return new Result<Void>().ok(null);
    }

}