package cn.laifuzhi.joymq.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum RespTypeEnum {
    OK((byte) 0, "success"),
    ;
    private static Map<Byte, RespTypeEnum> typeEnumMap = new HashMap<>();

    private byte type;
    private String decs;

    static {
        for (RespTypeEnum respTypeEnum : values()) {
            typeEnumMap.put(respTypeEnum.getType(), respTypeEnum);
        }
    }

    public static RespTypeEnum getByType(byte type) {
        return typeEnumMap.get(type);
    }

    public static boolean contains(byte type) {
        return typeEnumMap.containsKey(type);
    }
}
