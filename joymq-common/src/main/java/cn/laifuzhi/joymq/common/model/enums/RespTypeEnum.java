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
    PARAM_ERROR((byte) -2, "parameter error"),
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
