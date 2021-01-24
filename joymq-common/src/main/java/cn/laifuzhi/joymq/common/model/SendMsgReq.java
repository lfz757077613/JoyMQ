package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.FlushTypeEnum;
import cn.laifuzhi.joymq.common.utils.UtilAll;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMsgReq extends BaseInfoReq {
    private static final int MAX_TOPIC_LENGTH = 64;
    private static final int MAX_BODY_LENGTH = 3 * 1024 * 1024;
    private String topic;
    private byte flushType;
    private byte[] body;
    private int bodyCRC;

    // decode时保留原始ByteBuf的引用
    private ByteBuf origin;

    public SendMsgReq(String group, String topic, FlushTypeEnum flushType, byte[] body) {
        super(DataTypeEnum.SEND_MSG_REQ, group);
        if (body == null || body.length > MAX_BODY_LENGTH
                || topic == null || topic.length() > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.topic = topic;
        this.flushType = flushType.getType();
        this.body = body;
        this.bodyCRC = UtilAll.crc32(body);
    }

    @Override
    public SendMsgReq decode(ByteBuf byteBuf) {
        super.decode(byteBuf);
        short topicLength = byteBuf.readShort();
        this.topic = byteBuf.readCharSequence(topicLength, StandardCharsets.UTF_8).toString();
        this.flushType = byteBuf.readByte();
        this.body = new byte[byteBuf.readInt()];
        byteBuf = byteBuf.readBytes(this.body);
        this.bodyCRC = byteBuf.readInt();
        this.origin = byteBuf.resetReaderIndex().retain();
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        super.encode(byteBuf);
        byte[] topicBytes = this.topic.getBytes(StandardCharsets.UTF_8);
        byteBuf = byteBuf.writeShort(topicBytes.length).writeBytes(topicBytes);
        byteBuf = byteBuf.writeByte(this.flushType);
        byteBuf = byteBuf.writeInt(this.body.length).writeBytes(this.body);
        byteBuf = byteBuf.writeInt(this.bodyCRC);
        return byteBuf;
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.SEND_MSG_REQ;
    }
}
