package cn.laifuzhi.joymq.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum FlushTypeEnum {
    SYNC((byte) 0, "sync"),
    ASYNC((byte) 1, "async"),
    ;
    private static Map<Byte, FlushTypeEnum> typeEnumMap = new HashMap<>();

    private byte type;
    private String decs;

    static {
        for (FlushTypeEnum flushTypeEnum : values()) {
            typeEnumMap.put(flushTypeEnum.getType(), flushTypeEnum);
        }
    }

    public static FlushTypeEnum getByType(byte type) {
        return typeEnumMap.get(type);
    }

    public static boolean contains(byte type) {
        return typeEnumMap.containsKey(type);
    }
}
