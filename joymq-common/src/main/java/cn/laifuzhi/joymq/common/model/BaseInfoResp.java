package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import cn.laifuzhi.joymq.common.utils.UtilAll;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BaseInfoResp implements JoyMQModel {
    private static final int MAX_RESP_FROM_LENGTH = 64;
    private byte dataType;
    private int dataId;
    private byte respType;
    private String respFrom;

    BaseInfoResp(DataTypeEnum dataType, int dataId, RespTypeEnum respType) {
        this.dataType = dataType.getType();
        this.dataId = dataId;
        this.respType = respType.getType();
        this.respFrom = UtilAll.getFrom();
    }

    @Override
    public BaseInfoResp decode(ByteBuf byteBuf) {
        this.dataType = byteBuf.readByte();
        this.dataId = byteBuf.readInt();
        byteBuf = byteBuf.readSlice(byteBuf.readShort());
        this.respType = byteBuf.readByte();
        this.respFrom = byteBuf.readCharSequence(byteBuf.readShort(), StandardCharsets.UTF_8).toString();
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        byteBuf = byteBuf.writeByte(this.dataType);
        byteBuf = byteBuf.writeInt(this.dataId);
        int baseLengthWriterIndex = byteBuf.writerIndex();
        byteBuf = byteBuf.writerIndex(baseLengthWriterIndex + Short.BYTES);
        byteBuf = byteBuf.writeByte(this.respType);
        byte[] fromBytes = this.respFrom.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(fromBytes.length).writeBytes(fromBytes);
        byteBuf = byteBuf.setShort(baseLengthWriterIndex, byteBuf.writerIndex() - baseLengthWriterIndex - Short.BYTES);
        return byteBuf;
    }
}
