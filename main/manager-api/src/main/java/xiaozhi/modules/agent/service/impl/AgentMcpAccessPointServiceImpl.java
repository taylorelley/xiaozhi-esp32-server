package xiaozhi.modules.agent.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.utils.AESUtils;
import xiaozhi.common.utils.HashEncryptionUtil;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.modules.agent.Enums.XiaoZhiMcpJsonRpcJson;
import xiaozhi.modules.agent.service.AgentMcpAccessPointService;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.sys.utils.WebSocketClientManager;

@AllArgsConstructor
@Service
@Slf4j
public class AgentMcpAccessPointServiceImpl implements AgentMcpAccessPointService {
    private SysParamsService sysParamsService;

    @Override
    public String getAgentMcpAccessAddress(String id) {
        // gettomcp Address
        String url = sysParamsService.getValue(Constant.SERVER_MCP_ENDPOINT, true);
        if (StringUtils.isBlank(url) || "null".equals(url)) {
            return null;
        }
        URI uri = getURI(url);
        // getagentmcp url前缀
        String agentMcpUrl = getAgentMcpUrl(uri);
        // get key
        String key = getSecretKey(uri);
        // getencrypt token
        String encryptToken = encryptToken(id, key);
        // fortokenperformURLcode
        String encodedToken = URLEncoder.encode(encryptToken, StandardCharsets.UTF_8);
        // returnagentMcppath format
        agentMcpUrl = "%s/mcp/?token=%s".formatted(agentMcpUrl, encodedToken);
        return agentMcpUrl;
    }

    @Override
    public List<String> getAgentMcpToolsList(String id) {
        String wsUrl = getAgentMcpAccessAddress(id);
        if (StringUtils.isBlank(wsUrl)) {
            return List.of();
        }

        // will /mcp 替换as /call
        wsUrl = wsUrl.replace("/mcp/", "/call/");

        try {
            // create WebSocket connection，增加timeouttimeto15seconds
            try (WebSocketClientManager client = WebSocketClientManager.build(
                    new WebSocketClientManager.Builder()
                            .uri(wsUrl)
                            .bufferSize(1024 * 1024)
                            .connectTimeout(8, TimeUnit.SECONDS)
                            .maxSessionDuration(10, TimeUnit.SECONDS))) {

                // 步骤1: sendinitializemessage并waitresponse
                log.info("sendMCPinitializemessage，Agent ID: {}", id);
                client.sendText(XiaoZhiMcpJsonRpcJson.getInitializeJson());

                // waitinitializeresponse (id=1) - 移除固定延迟，改asresponse驱动
                List<String> initResponses = client.listenerWithoutClose(response -> {
                    try {
                        Map<String, Object> jsonMap = JsonUtils.parseObject(response, Map.class);
                        if (jsonMap != null && Integer.valueOf(1).equals(jsonMap.get("id"))) {
                            // 检查YesNo有resultfield，representsinitializesuccess
                            return jsonMap.containsKey("result") && !jsonMap.containsKey("error");
                        }
                        return false;
                    } catch (Exception e) {
                        log.warn("parseinitializeresponsefailed: {}", response, e);
                        return false;
                    }
                });

                // verificationinitializeresponse
                boolean initSucceeded = false;
                for (String response : initResponses) {
                    try {
                        Map<String, Object> jsonMap = JsonUtils.parseObject(response, Map.class);
                        if (jsonMap != null && Integer.valueOf(1).equals(jsonMap.get("id"))) {
                            if (jsonMap.containsKey("result")) {
                                log.info("MCPinitializesuccess，Agent ID: {}", id);
                                initSucceeded = true;
                                break;
                            } else if (jsonMap.containsKey("error")) {
                                log.error("MCPinitializefailed，Agent ID: {}, error: {}", id, jsonMap.get("error"));
                                return List.of();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("processinitializeresponsefailed: {}", response, e);
                    }
                }

                if (!initSucceeded) {
                    log.error("not收tovalid MCPinitializeresponse，Agent ID: {}", id);
                    return List.of();
                }

                // 步骤2: sendinitializecomplete通知 - only有in收toinitializeresponse后才send
                log.info("sendMCPinitializecomplete通知，Agent ID: {}", id);
                client.sendText(XiaoZhiMcpJsonRpcJson.getNotificationsInitializedJson());
                // 步骤3: sendtool listrequest - immediatelysend，无需额外延迟
                log.info("sendMCP tool listrequest，Agent ID: {}", id);
                client.sendText(XiaoZhiMcpJsonRpcJson.getToolsListJson());

                // waittool listresponse (id=2)
                List<String> toolsResponses = client.listener(response -> {
                    try {
                        Map<String, Object> jsonMap = JsonUtils.parseObject(response, Map.class);
                        return jsonMap != null && Integer.valueOf(2).equals(jsonMap.get("id"));
                    } catch (Exception e) {
                        log.warn("parsetool listresponsefailed: {}", response, e);
                        return false;
                    }
                });

                // processtool listresponse
                for (String response : toolsResponses) {
                    try {
                        Map<String, Object> jsonMap = JsonUtils.parseObject(response, Map.class);
                        if (jsonMap != null && Integer.valueOf(2).equals(jsonMap.get("id"))) {
                            // 检查YesNo有resultfield
                            Object resultObj = jsonMap.get("result");
                            if (resultObj instanceof Map) {
                                Map<String, Object> resultMap = (Map<String, Object>) resultObj;
                                Object toolsObj = resultMap.get("tools");
                                if (toolsObj instanceof List) {
                                    List<Map<String, Object>> toolsList = (List<Map<String, Object>>) toolsObj;
                                    // extracttoolnamelist
                                    List<String> result = toolsList.stream()
                                            .map(tool -> (String) tool.get("name"))
                                            .filter(name -> name != null)
                                            .sorted()
                                            .collect(Collectors.toList());
                                    log.info("successgetMCP tool list，Agent ID: {}, toolcount: {}", id, result.size());
                                    return result;
                                }
                            } else if (jsonMap.containsKey("error")) {
                                log.error("gettool listfailed，Agent ID: {}, error: {}", id, jsonMap.get("error"));
                                return List.of();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("processtool listresponsefailed: {}", response, e);
                    }
                }

                log.warn("not foundvalid tool listresponse，Agent ID: {}", id);
                return List.of();

            }
        } catch (Exception e) {
            log.error("getagent MCP tool listfailed，Agent ID: {},errorreason：{}", id, e.getMessage());
            return List.of();
        }
    }

    /**
     * getURIobject
     * 
     * @param url path
     * @return URIobject
     */
    private static URI getURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            log.error("pathformat is incorrectpath：{}，\nerrorinformation:{}", url, e.getMessage());
            throw new RuntimeException("mcp Address存inerror，请进入Parameter managementupdatemcpendpointAddress");
        }
    }

    /**
     * get key
     *
     * @param uri mcpAddress
     * @return key
     */
    private static String getSecretKey(URI uri) {
        // getparameter
        String query = uri.getQuery();
        // getaesencryption key
        String str = "key=";
        return query.substring(query.indexOf(str) + str.length());
    }

    /**
     * getagentmcpendpointurl
     *
     * @param uri mcpAddress
     * @return agentmcpendpointurl
     */
    private String getAgentMcpUrl(URI uri) {
        // get协议
        String wsScheme = (uri.getScheme().equals("https")) ? "wss" : "ws";
        // get主机，end口，path
        String path = uri.getSchemeSpecificPart();
        // gettolast一个/前 path
        path = path.substring(0, path.lastIndexOf("/"));
        return wsScheme + ":" + path;
    }

    /**
     * getforagentidencrypt token
     *
     * @param agentId agentid
     * @param key     encryption key
     * @return encrypt后token
     */
    private static String encryptToken(String agentId, String key) {
        // usemd5foragentidperformencrypt
        String md5 = HashEncryptionUtil.Md5hexDigest(agentId);
        // aesneed加ciphertextthis
        String json = "{\"agentId\": \"%s\"}".formatted(md5);
        // encrypt后成tokenvalue
        return AESUtils.encrypt(key, json);
    }
}