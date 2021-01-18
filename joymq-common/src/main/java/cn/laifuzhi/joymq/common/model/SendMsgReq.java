package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.FlushTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMsgReq extends BaseInfo {
    private static final int MAX_TOPIC_LENGTH = 64;
    private static final int MAX_BODY_LENGTH = 3 * 1024 * 1024;
    private String topic;
    private FlushTypeEnum flushType;
    private int bodyCRC;
    private byte[] body;

    public SendMsgReq(String from, String group, String topic, byte[] body) {
        super(DataTypeEnum.SEND_MSG_REQ, from, group);
        if (body == null || body.length > MAX_BODY_LENGTH
                || topic == null || topic.length() > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.topic = topic;
        this.body = body;
    }

    @Override
    public SendMsgReq decode(ByteBuf byteBuf) {
        super.decode(byteBuf);
        int bodyLength = byteBuf.readInt();
        if (bodyLength > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.body = new byte[bodyLength];
        byteBuf.readBytes(this.body);
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        super.encode(byteBuf);
        return byteBuf.writeInt(this.body.length).writeBytes(this.body);
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.SEND_MSG_REQ;
    }
}
