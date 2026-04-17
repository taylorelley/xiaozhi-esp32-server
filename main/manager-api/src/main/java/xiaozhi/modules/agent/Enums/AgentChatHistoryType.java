package xiaozhi.modules.agent.Enums;


import lombok.Getter;

/**
 * agentChat historytype
 */
@Getter
public enum AgentChatHistoryType {

    USER((byte) 1),
    AGENT((byte) 2);

    private final byte value;

    AgentChatHistoryType(byte i) {
        this.value = i;
    }

}
