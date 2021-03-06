package cn.laifuzhi.joymq.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum FlushTypeEnum {
    ASYNC((byte) 0, "async"),
    SYNC((byte) 1, "sync"),
    HA((byte) 2, "HA"),
    ;
    private static Map<Byte, FlushTypeEnum> typeEnumMap = new HashMap<>();

    private byte type;
    private String decs;

    static {
        for (FlushTypeEnum flushTypeEnum : values()) {
            typeEnumMap.put(flushTypeEnum.getType(), flushTypeEnum);
        }
    }

    public static Optional<FlushTypeEnum> getByType(byte type) {
        return Optional.ofNullable(typeEnumMap.get(type));
    }

    public static boolean contains(byte type) {
        return typeEnumMap.containsKey(type);
    }
}
