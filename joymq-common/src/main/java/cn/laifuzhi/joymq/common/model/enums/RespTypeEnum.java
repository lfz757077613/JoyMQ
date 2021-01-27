package cn.laifuzhi.joymq.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum RespTypeEnum {
    OK((byte) 0, "success"),
    UNEXPECTED_ERROR((byte) -1, "unexpect error"),
    BROKER_BUSY((byte) -2, "broker busy"),
    DATA_TYPE_NOT_EXIST((byte) -3, "data type not exist"),
    DATA_TYPE_NOT_SUPPORT((byte) -4, "data type not support"),
    DECODE_ERROR((byte) -5, "decode error"),
    ;
    private static Map<Byte, RespTypeEnum> typeEnumMap = new HashMap<>();

    private byte type;
    private String decs;

    static {
        for (RespTypeEnum respTypeEnum : values()) {
            typeEnumMap.put(respTypeEnum.getType(), respTypeEnum);
        }
    }

    public static Optional<RespTypeEnum> getByType(byte type) {
        return Optional.ofNullable(typeEnumMap.get(type));
    }

    public static boolean contains(byte type) {
        return typeEnumMap.containsKey(type);
    }
}
