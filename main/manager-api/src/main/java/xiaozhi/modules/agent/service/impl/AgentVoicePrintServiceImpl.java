package xiaozhi.modules.agent.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.modules.agent.dao.AgentVoicePrintDao;
import xiaozhi.modules.agent.dto.AgentVoicePrintSaveDTO;
import xiaozhi.modules.agent.dto.AgentVoicePrintUpdateDTO;
import xiaozhi.modules.agent.dto.IdentifyVoicePrintResponse;
import xiaozhi.modules.agent.entity.AgentVoicePrintEntity;
import xiaozhi.modules.agent.service.AgentChatAudioService;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentVoicePrintService;
import xiaozhi.modules.agent.vo.AgentVoicePrintVO;
import xiaozhi.modules.sys.service.SysParamsService;

/**
 * @author zjy
 */
@Service
@Slf4j
public class AgentVoicePrintServiceImpl extends ServiceImpl<AgentVoicePrintDao, AgentVoicePrintEntity>
        implements AgentVoicePrintService {
    private final AgentChatAudioService agentChatAudioService;
    private final RestTemplate restTemplate;
    private final SysParamsService sysParamsService;
    private final AgentChatHistoryService agentChatHistoryService;
    // Springboot提供 编程transactionclass
    private final TransactionTemplate transactionTemplate;
    // 识别度
    private final Double RECOGNITION = 0.5;
    private final Executor taskExecutor;

    public AgentVoicePrintServiceImpl(AgentChatAudioService agentChatAudioService, RestTemplate restTemplate,
                                      SysParamsService sysParamsService, AgentChatHistoryService agentChatHistoryService,
                                      TransactionTemplate transactionTemplate, @Qualifier("taskExecutor") Executor taskExecutor) {
        this.agentChatAudioService = agentChatAudioService;
        this.restTemplate = restTemplate;
        this.sysParamsService = sysParamsService;
        this.agentChatHistoryService = agentChatHistoryService;
        this.transactionTemplate = transactionTemplate;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public boolean insert(AgentVoicePrintSaveDTO dto) {
        // getaudio data
        ByteArrayResource resource = getVoicePrintAudioWAV(dto.getAgentId(), dto.getAudioId());
        // 识别one下thisvoiceYesNoregister
        IdentifyVoicePrintResponse response = identifyVoicePrint(dto.getAgentId(), resource);
        if (response != null && response.getScore() > RECOGNITION) {
            // according to识别出 Voiceprint IDquerycorresponding User information
            AgentVoicePrintEntity existingVoicePrint = baseMapper.selectById(response.getSpeakerId());
            String existingUserName = existingVoicePrint != null ? existingVoicePrint.getSourceName() : "Unknownuser";
            throw new RenException(ErrorCode.VOICEPRINT_ALREADY_REGISTERED, existingUserName);
        }
        AgentVoicePrintEntity entity = ConvertUtils.sourceToTarget(dto, AgentVoicePrintEntity.class);
        // start transaction
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // saveVoiceprint information
                int row = baseMapper.insert(entity);
                // insertoneitemsdata，影响 datanot etc.于1Description出现，savequestion回滚
                if (row != 1) {
                    status.setRollbackOnly(); // mark transaction rollback
                    return false;
                }
                // sendregistervoiceprintrequest
                registerVoicePrint(entity.getId(), resource);
                return true;
            } catch (RenException e) {
                status.setRollbackOnly(); // mark transaction rollback
                throw e;
            } catch (Exception e) {
                status.setRollbackOnly(); // mark transaction rollback
                log.error("savevoiceprinterrorreason：{}", e.getMessage());
                throw new RenException(ErrorCode.VOICE_PRINT_SAVE_ERROR);
            }
        }));
    }

    @Override
    public boolean delete(Long userId, String voicePrintId) {
        // start transaction
        boolean b = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // deletevoiceprint,byaccording tospecifiedcurrently logged-inuserandagent
                int row = baseMapper.delete(new LambdaQueryWrapper<AgentVoicePrintEntity>()
                        .eq(AgentVoicePrintEntity::getId, voicePrintId)
                        .eq(AgentVoicePrintEntity::getCreator, userId));
                if (row != 1) {
                    status.setRollbackOnly(); // mark transaction rollback
                    return false;
                }

                return true;
            } catch (Exception e) {
                status.setRollbackOnly(); // mark transaction rollback
                log.error("deletevoiceprintstoreinerrorreason：{}", e.getMessage());
                throw new RenException(ErrorCode.VOICEPRINT_DELETE_ERROR);
            }
        }));
        // datalibraryvoiceprintdatadeletesuccessonly继续executedeletevoiceprintservice data
        if(b){
            taskExecutor.execute(()-> {
                try {
                    cancelVoicePrint(voicePrintId);
                }catch (RuntimeException e) {
                    log.error("deletevoiceprintstoreinrunwhenerrorreason：{}，id：{}", e.getMessage(),voicePrintId);
                }
            });
        }
        return b;
    }

    @Override
    public List<AgentVoicePrintVO> list(Long userId, String agentId) {
        // byaccording tospecifiedcurrently logged-inuserandagentfinddata
        List<AgentVoicePrintEntity> list = baseMapper.selectList(new LambdaQueryWrapper<AgentVoicePrintEntity>()
                .eq(AgentVoicePrintEntity::getAgentId, agentId)
                .eq(AgentVoicePrintEntity::getCreator, userId));
        return list.stream().map(entity -> {
            // 遍历convert成AgentVoicePrintVOtype
            return ConvertUtils.sourceToTarget(entity, AgentVoicePrintVO.class);
        }).toList();

    }

    @Override
    public boolean update(Long userId, AgentVoicePrintUpdateDTO dto) {
        AgentVoicePrintEntity agentVoicePrintEntity = baseMapper
                .selectOne(new LambdaQueryWrapper<AgentVoicePrintEntity>()
                        .eq(AgentVoicePrintEntity::getId, dto.getId())
                        .eq(AgentVoicePrintEntity::getCreator, userId));
        if (agentVoicePrintEntity == null) {
            return false;
        }
        // getaudioId
        String audioId = dto.getAudioId();
        // getagentid
        String agentId = agentVoicePrintEntity.getAgentId();
        ByteArrayResource resource;
        // audioIdnot etc.于empty，andaudioIdand之before save audioidnot one样，thenneedre-newgetaudio datageneratevoiceprint
        if (!StringUtils.isEmpty(audioId) && !audioId.equals(agentVoicePrintEntity.getAudioId())) {
            resource = getVoicePrintAudioWAV(agentId, audioId);

            // 识别one下thisvoiceYesNoregister
            IdentifyVoicePrintResponse response = identifyVoicePrint(agentId, resource);
            // return分number高于RECOGNITIONDescriptionthisvoiceprintalready经has
            if (response != null && response.getScore() > RECOGNITION) {
                // determinereturn idifnot Yesneed toupdate voiceprintid，Descriptionthisvoiceprintid，现inneed toregister voicealready经storeinandnot Yes原来 voiceprint，not allowupdate
                if (!response.getSpeakerId().equals(dto.getId())) {
                    // according to识别出 Voiceprint IDquerycorresponding User information
                    AgentVoicePrintEntity existingVoicePrint = baseMapper.selectById(response.getSpeakerId());
                    String existingUserName = existingVoicePrint != null ? existingVoicePrint.getSourceName() : "Unknownuser";
                    throw new RenException(ErrorCode.VOICEPRINT_UPDATE_NOT_ALLOWED, existingUserName);
                }
            }
        } else {
            resource = null;
        }
        // start transaction
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                AgentVoicePrintEntity entity = ConvertUtils.sourceToTarget(dto, AgentVoicePrintEntity.class);
                int row = baseMapper.updateById(entity);
                if (row != 1) {
                    status.setRollbackOnly(); // mark transaction rollback
                    return false;
                }
                if (resource != null) {
                    String id = entity.getId();
                    // first注销之beforethisvoiceprintid上 voiceprintvector
                    cancelVoicePrint(id);
                    // sendregistervoiceprintrequest
                    registerVoicePrint(id, resource);
                }
                return true;
            } catch (RenException e) {
                status.setRollbackOnly(); // mark transaction rollback
                throw e;
            } catch (Exception e) {
                status.setRollbackOnly(); // mark transaction rollback
                log.error("updatevoiceprinterrorreason：{}", e.getMessage());
                throw new RenException(ErrorCode.VOICEPRINT_UPDATE_ADMIN_ERROR);
            }
        }));
    }

    /**
     * get生纹interfaceURIobject
     *
     * @return URIobject
     */
    private URI getVoicePrintURI() {
        // getVoiceprint interface address
        String voicePrint = sysParamsService.getValue(Constant.SERVER_VOICE_PRINT, true);
        try {
            return new URI(voicePrint);
        } catch (URISyntaxException e) {
            log.error("pathformat is incorrectpath：{}，\nerrorinformation:{}", voicePrint, e.getMessage());
                throw new RenException(ErrorCode.VOICEPRINT_API_URI_ERROR);
        }
    }

    /**
     * getvoiceprintAddressbasepath
     * 
     * @param uri voiceprintAddressuri
     * @return basepath
     */
    private String getBaseUrl(URI uri) {
        String protocol = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            return "%s://%s".formatted(protocol, host);
        } else {
            return "%s://%s:%s".formatted(protocol, host, port);
        }
    }

    /**
     * getverificationAuthorization
     *
     * @param uri voiceprintAddressuri
     * @return Authorizationvalue
     */
    private String getAuthorization(URI uri) {
        // getparameter
        String query = uri.getQuery();
        // getaesencryption key
        String str = "key=";
        return "Bearer " + query.substring(query.indexOf(str) + str.length());
    }

    /**
     * getvoiceprintaudioresourcesourcedata
     *
     * @param audioId audioId
     * @return voiceprintaudioresourcesourcedata
     */
    private ByteArrayResource getVoicePrintAudioWAV(String agentId, String audioId) {
        // determinethisaudioYesNobelongs tocurrentagent
        boolean b = agentChatHistoryService.isAudioOwnedByAgent(audioId, agentId);
        if (!b) {
            throw new RenException(ErrorCode.VOICEPRINT_AUDIO_NOT_BELONG_AGENT);
        }
        // gettoaudio data
        byte[] audio = agentChatAudioService.getAudio(audioId);
        // ifAudio data is empty directly报错not perform下去
        if (audio == null || audio.length == 0) {
            throw new RenException(ErrorCode.VOICEPRINT_AUDIO_EMPTY);
        }
        // willbytearray包装asresourcesource，return
        return new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "VoicePrint.WAV"; // setFile name
            }
        };
    }

    /**
     * sendregistervoiceprinthttprequest
     * 
     * @param id       voiceprintid
     * @param resource voiceprintaudioresourcesource
     */
    private void registerVoicePrint(String id, ByteArrayResource resource) {
        // processVoiceprint interface address，getbefore缀
        URI uri = getVoicePrintURI();
        String baseUrl = getBaseUrl(uri);
        String requestUrl = baseUrl + "/voiceprint/register";
        // createrequest
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("speaker_id", id);
        body.add("file", resource);

        // createrequestheader
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorization(uri));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // createrequest
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        // send POST request
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, requestEntity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Voiceprint registrationfailed,requestpath：{}", requestUrl);
            throw new RenException(ErrorCode.VOICEPRINT_REGISTER_REQUEST_ERROR);
        }
        // checkresponsecontent
        String responseBody = response.getBody();
        if (responseBody == null || !responseBody.contains("true")) {
            log.error("Voiceprint registrationfailed,requestprocessfailedcontent：{}", responseBody == null ? "emptycontent" : responseBody);
            throw new RenException(ErrorCode.VOICEPRINT_REGISTER_PROCESS_ERROR);
        }
    }

    /**
     * send注销voiceprint request
     * 
     * @param voicePrintId voiceprintid
     */
    private void cancelVoicePrint(String voicePrintId) {
        URI uri = getVoicePrintURI();
        String baseUrl = getBaseUrl(uri);
        String requestUrl = baseUrl + "/voiceprint/" + voicePrintId;
        // createrequestheader
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorization(uri));
        // createrequest
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);

        // send POST request
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.DELETE, requestEntity,
                String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Voiceprint cancellationfailed,requestpath：{}", requestUrl);
            throw new RenException(ErrorCode.VOICEPRINT_UNREGISTER_REQUEST_ERROR);
        }
        // checkresponsecontent
        String responseBody = response.getBody();
        if (responseBody == null || !responseBody.contains("true")) {
            log.error("Voiceprint cancellationfailed,requestprocessfailedcontent：{}", responseBody == null ? "emptycontent" : responseBody);
            throw new RenException(ErrorCode.VOICEPRINT_UNREGISTER_PROCESS_ERROR);
        }
    }

    /**
     * send识别voiceprinthttprequest
     * 
     * @param agentId  agentid
     * @param resource voiceprintaudioresourcesource
     * @return return识别data
     */
    private IdentifyVoicePrintResponse identifyVoicePrint(String agentId, ByteArrayResource resource) {

        // getthisagentallregister voiceprint
        List<AgentVoicePrintEntity> agentVoicePrintList = baseMapper
                .selectList(new LambdaQueryWrapper<AgentVoicePrintEntity>()
                        .select(AgentVoicePrintEntity::getId)
                        .eq(AgentVoicePrintEntity::getAgentId, agentId));

        // voiceprintcountas0，Descriptionstill没registervoiceprintnot need发生识别request
        if (agentVoicePrintList.isEmpty()) {
            return null;
        }
        // processVoiceprint interface address，getbefore缀
        URI uri = getVoicePrintURI();
        String baseUrl = getBaseUrl(uri);
        String requestUrl = baseUrl + "/voiceprint/identify";
        // createrequest
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // createspeaker_idparameter
        String speakerIds = agentVoicePrintList.stream()
                .map(AgentVoicePrintEntity::getId)
                .collect(Collectors.joining(","));
        body.add("speaker_ids", speakerIds);
        body.add("file", resource);

        // createrequestheader
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorization(uri));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // createrequest
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        // send POST request
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, requestEntity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Voiceprint identification request failed,requestpath：{}", requestUrl);
            throw new RenException(ErrorCode.VOICEPRINT_IDENTIFY_REQUEST_ERROR);
        }
        // checkresponsecontent
        String responseBody = response.getBody();
        if (responseBody != null) {
            return JsonUtils.parseObject(responseBody, IdentifyVoicePrintResponse.class);
        }
        return null;
    }
}
