package cn.laifuzhi.joymq.common.model.enums;

import cn.laifuzhi.joymq.common.model.JoyMQModel;
import cn.laifuzhi.joymq.common.model.Ping;
import cn.laifuzhi.joymq.common.model.Pong;
import cn.laifuzhi.joymq.common.model.SendMsgReq;
import cn.laifuzhi.joymq.common.model.SendMsgResp;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {
    SYSTEM_RESP((byte) 0, Ping.class, "system response"),
    PING((byte) 1, Ping.class, "heartbeat request"),
    PONG((byte) 2, Pong.class, "heartbeat response"),
    SEND_MSG_REQ((byte) 3, SendMsgReq.class, "send message request"),
    SEND_MSG_RESP((byte) 4, SendMsgResp.class, "send message response"),
    ;
    private static Map<Byte, DataTypeEnum> typeEnumMap = new HashMap<>();
    private byte type;
    private Class<? extends JoyMQModel> mqModelClass;
    private String desc;

    static {
        for (DataTypeEnum dataTypeEnum : values()) {
            typeEnumMap.put(dataTypeEnum.getType(), dataTypeEnum);
        }
    }

    public static Optional<DataTypeEnum> getByType(byte type) {
        return Optional.ofNullable(typeEnumMap.get(type));
    }

    public static boolean contains(byte type) {
        return typeEnumMap.containsKey(type);
    }
}
