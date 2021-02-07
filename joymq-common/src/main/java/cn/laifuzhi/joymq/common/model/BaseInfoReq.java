package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.utils.UtilAll;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BaseInfoReq implements JoyMQDTO {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private static final int MAX_GROUP_LENGTH = 64;
    private byte dataType;
    private int dataId;
    private String reqFrom;
    private String group;

    BaseInfoReq(DataTypeEnum dataType, String group) {
        if (group == null || group.length() > MAX_GROUP_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.dataType = dataType.getType();
        this.dataId = ID_GENERATOR.incrementAndGet();
        this.reqFrom = UtilAll.getFrom();
        this.group = group;
    }

    @Override
    public BaseInfoReq decode(ByteBuf byteBuf) {
        this.dataType = byteBuf.readByte();
        this.dataId = byteBuf.readInt();
        byteBuf = byteBuf.readSlice(byteBuf.readShort());
        this.reqFrom = byteBuf.readCharSequence(byteBuf.readShort(), StandardCharsets.UTF_8).toString();
        this.group = byteBuf.readCharSequence(byteBuf.readShort(), StandardCharsets.UTF_8).toString();
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        byteBuf = byteBuf.writeByte(this.dataType);
        byteBuf = byteBuf.writeInt(this.dataId);
        int baseLengthWriterIndex = byteBuf.writerIndex();
        byteBuf = byteBuf.writerIndex(baseLengthWriterIndex + Short.BYTES);
        byte[] fromBytes = this.reqFrom.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(fromBytes.length).writeBytes(fromBytes);
        byte[] groupBytes = this.group.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(groupBytes.length).writeBytes(groupBytes);
        byteBuf = byteBuf.setShort(baseLengthWriterIndex, byteBuf.writerIndex() - baseLengthWriterIndex - Short.BYTES);
        return byteBuf;
    }
}
