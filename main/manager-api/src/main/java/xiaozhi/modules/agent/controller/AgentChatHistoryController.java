package xiaozhi.modules.agent.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.DateUtils;
import xiaozhi.common.utils.MessageUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.agent.dto.AgentChatHistoryDTO;
import xiaozhi.modules.agent.dto.AgentChatHistoryReportDTO;
import xiaozhi.modules.agent.dto.AgentChatSessionDTO;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.service.biz.AgentChatHistoryBizService;
import xiaozhi.modules.security.user.SecurityUser;

@Tag(name = "Agent chat historymanagement")
@RequiredArgsConstructor
@RestController
@RequestMapping("/agent/chat-history")
public class AgentChatHistoryController {
    private final AgentChatHistoryBizService agentChatHistoryBizService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentService agentService;
    private final RedisUtils redisUtils;

    /**
     * LittleWiseservicechat reportingrequest
     * <p>
     * LittleWiseservicechat reportingrequest，containBase64code audio dataandrelatedinformation。
     *
     * @param request containuploadfile及relatedinformation requestobject
     */
    @Operation(summary = "LittleWiseservicechat reportingrequest")
    @PostMapping("/report")
    public Result<Boolean> uploadFile(@Valid @RequestBody AgentChatHistoryReportDTO request) {
        Boolean result = agentChatHistoryBizService.report(request);
        return new Result<Boolean>().ok(result);
    }

    /**
     * getChat historydownloadlink
     * 
     * @param agentId   Agent ID
     * @param sessionId Session ID
     * @return UUIDasdownloadidentifier
     */
    @Operation(summary = "getChat historydownloadlink")
    @RequiresPermissions("sys:role:normal")
    @PostMapping("/getDownloadUrl/{agentId}/{sessionId}")
    public Result<String> getDownloadUrl(@PathVariable("agentId") String agentId,
            @PathVariable("sessionId") String sessionId) {
        // get currentuser
        UserDetail user = SecurityUser.getUser();
        // checkPermission
        if (!agentService.checkAgentPermission(agentId, user.getId())) {
            throw new RenException(ErrorCode.CHAT_HISTORY_NO_PERMISSION);
        }

        // generateUUID
        String uuid = UUID.randomUUID().toString();
        // store储agentIdandsessionIdtoRedis，formatasagentId:sessionId
        redisUtils.set(RedisKeys.getChatHistoryKey(uuid), agentId + ":" + sessionId);

        return new Result<String>().ok(uuid);
    }

    /**
     * downloadthissessionChat history
     * 
     * @param uuid     downloadidentifier
     * @param response HTTPresponse
     */
    @Operation(summary = "downloadthissessionChat history")
    @GetMapping("/download/{uuid}/current")
    public void downloadCurrentSession(@PathVariable("uuid") String uuid,
            HttpServletResponse response) {
        // fromRedisgetagentIdandsessionId
        String agentSessionInfo = (String) redisUtils.get(RedisKeys.getChatHistoryKey(uuid));
        if (StringUtils.isBlank(agentSessionInfo)) {
            throw new RenException(ErrorCode.DOWNLOAD_LINK_EXPIRED);
        }

        try {
            // parseagentIdandsessionId
            String[] parts = agentSessionInfo.split(":");
            if (parts.length != 2) {
                throw new RenException(ErrorCode.DOWNLOAD_LINK_INVALID);
            }
            String agentId = parts[0];
            String sessionId = parts[1];

            // executedownload
            downloadChatHistory(agentId, List.of(sessionId), response);
        } finally {
            // downloadcompleteafterdeleteUUID，prevent盗刷
            redisUtils.delete(RedisKeys.getChatHistoryKey(uuid));
        }
    }

    /**
     * downloadthissession及before20itemssessionChat history
     * 
     * @param uuid     downloadidentifier
     * @param response HTTPresponse
     */
    @Operation(summary = "downloadthissession及before20itemssessionChat history")
    @GetMapping("/download/{uuid}/previous")
    public void downloadCurrentSessionWithPrevious(@PathVariable("uuid") String uuid,
            HttpServletResponse response) {
        // fromRedisgetagentIdandsessionId
        String agentSessionInfo = (String) redisUtils.get(RedisKeys.getChatHistoryKey(uuid));
        if (StringUtils.isBlank(agentSessionInfo)) {
            throw new RenException(ErrorCode.DOWNLOAD_LINK_EXPIRED);
        }

        try {
            // parseagentIdandsessionId
            String[] parts = agentSessionInfo.split(":");
            if (parts.length != 2) {
                throw new RenException(ErrorCode.DOWNLOAD_LINK_INVALID);
            }
            String agentId = parts[0];
            String sessionId = parts[1];

            // get allsessionlist
            Map<String, Object> params = Map.of(
                    "agentId", agentId,
                    Constant.PAGE, 1,
                    Constant.LIMIT, 1000 // get足够多 session
            );
            PageData<AgentChatSessionDTO> sessionPage = agentChatHistoryService.getSessionListByAgentId(params);
            List<AgentChatSessionDTO> allSessions = sessionPage.getList();

            // findcurrentsessioninlist bit置
            int currentIndex = -1;
            for (int i = 0; i < allSessions.size(); i++) {
                if (allSessions.get(i).getSessionId().equals(sessionId)) {
                    currentIndex = i;
                    break;
                }
            }

            // iffindtocurrentsession，收collectioncurrentsession及before20itemsSession ID
            List<String> sessionIdsToDownload = new ArrayList<>();
            if (currentIndex != -1) {
                // fromcurrentsessionstart，向after（arrayafter面）取most多20itemssession（包括currentsession）
                int endIndex = Math.min(allSessions.size() - 1, currentIndex + 20); // ensurenot 越界
                for (int i = currentIndex; i <= endIndex; i++) {
                    sessionIdsToDownload.add(allSessions.get(i).getSessionId());
                }
            }

            // ifnofindtocurrentsession，至少downloadcurrentsession
            if (sessionIdsToDownload.isEmpty()) {
                sessionIdsToDownload.add(sessionId);
            }
            downloadChatHistory(agentId, sessionIdsToDownload, response);
        } finally {
            // downloadcompleteafterdeleteUUID，prevent盗刷
            redisUtils.delete(RedisKeys.getChatHistoryKey(uuid));
        }
    }

    /**
     * downloadspecifiedsession Chat history
     * 
     * @param agentId    Agent ID
     * @param sessionIds Session IDlist
     * @param response   HTTPresponse
     */
    private void downloadChatHistory(String agentId, List<String> sessionIds, HttpServletResponse response) {
        try {
            // setresponseheader
            response.setContentType("text/plain;charset=UTF-8");
            String fileName = URLEncoder.encode("history.txt", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // getChat historyandwrite入response流
            try (OutputStream out = response.getOutputStream()) {
                // as每sessiongenerateChat history
                for (String sessionId : sessionIds) {
                    // getthissession allChat history
                    List<AgentChatHistoryDTO> chatHistoryList = agentChatHistoryService
                            .getChatHistoryBySessionId(agentId, sessionId);

                    // fromChat historyget第oneitemsmessage Create timeassessiontime
                    if (!chatHistoryList.isEmpty()) {
                        Date firstMessageTime = chatHistoryList.get(0).getCreatedAt();
                        String sessionTimeStr = DateUtils.format(firstMessageTime, DateUtils.DATE_TIME_PATTERN);
                        out.write((sessionTimeStr + "\n").getBytes(StandardCharsets.UTF_8));
                    }

                    for (AgentChatHistoryDTO message : chatHistoryList) {
                        String role = message.getChatType() == 1 ? MessageUtils.getMessage(ErrorCode.CHAT_ROLE_USER)
                                : MessageUtils.getMessage(ErrorCode.CHAT_ROLE_AGENT);
                        String direction = message.getChatType() == 1 ? ">>" : "<<";
                        Date messageTime = message.getCreatedAt();
                        String messageTimeStr = DateUtils.format(messageTime, DateUtils.DATE_TIME_PATTERN);
                        String content = message.getContent();

                        String line = "[" + role + "]-[" + messageTimeStr + "]" + direction + ":" + content + "\n";
                        out.write(line.getBytes(StandardCharsets.UTF_8));
                    }

                    // session之间addemptyrow分隔
                    if (sessionIds.indexOf(sessionId) < sessionIds.size() - 1) {
                        out.write("\n".getBytes(StandardCharsets.UTF_8));
                    }
                }

                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
