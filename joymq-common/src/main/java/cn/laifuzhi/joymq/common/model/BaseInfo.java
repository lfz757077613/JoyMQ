package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
abstract class BaseInfo implements JoyMQModel {
    private static final AtomicInteger DATAID_GENERATOR = new AtomicInteger();
    private static final int MAX_FROM_LENGTH = 64;
    private static final int MAX_GROUP_LENGTH = 64;
    private DataTypeEnum type;
    private String from;
    private int dataId;
    private String group;

    BaseInfo(DataTypeEnum type, String from, String group) {
        if (from == null || from.length() > MAX_FROM_LENGTH
                || group == null || group.length() > MAX_GROUP_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.from = from;
        this.dataId = DATAID_GENERATOR.incrementAndGet();
        this.group = group;
    }

    BaseInfo(DataTypeEnum type, String from, int dataId, String group) {
        if (from == null || from.length() > MAX_FROM_LENGTH
                || group == null || group.length() > MAX_GROUP_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.from = from;
        this.dataId = dataId;
        this.group = group;
    }

    @Override
    public BaseInfo decode(ByteBuf byteBuf) {
        this.type = DataTypeEnum.getByType(byteBuf.readByte());
        if (this.type == null) {
            throw new IllegalArgumentException();
        }
        short baseLength = byteBuf.readShort();
        byteBuf = byteBuf.readSlice(baseLength);
        short fromLength = byteBuf.readShort();
        this.from = byteBuf.readCharSequence(fromLength, StandardCharsets.UTF_8).toString();
        if (this.from.length() > MAX_FROM_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.dataId = byteBuf.readInt();
        short groupLength = byteBuf.readShort();
        this.group = byteBuf.readCharSequence(groupLength, StandardCharsets.UTF_8).toString();
        if (this.group.length() > MAX_GROUP_LENGTH) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        byteBuf = byteBuf.writeByte(this.type.getType());
        byteBuf = byteBuf.writerIndex(Byte.BYTES + Short.BYTES);
        byte[] fromBytes = this.from.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(fromBytes.length).writeBytes(fromBytes);
        byteBuf = byteBuf.writeInt(this.dataId);
        byte[] groupBytes = this.group.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(groupBytes.length).writeBytes(groupBytes);
        return byteBuf.setShort(Byte.BYTES, byteBuf.readableBytes() - Short.BYTES - Byte.BYTES);
    }
}
