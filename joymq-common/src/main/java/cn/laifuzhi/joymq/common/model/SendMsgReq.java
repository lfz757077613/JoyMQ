package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMsgReq extends BaseInfo {
    private byte[] body;

    public SendMsgReq(String from, String group, byte[] body) {
        super(DataTypeEnum.SEND_MSG_REQ, from, group);
        if (body == null) {
            throw new IllegalArgumentException();
        }
        this.body = body;
    }

    @Override
    public SendMsgReq decode(ByteBuf byteBuf) {
        super.decode(byteBuf);
        int bodyLength = byteBuf.readInt();
        if (bodyLength >= 0) {
            this.body = new byte[bodyLength];
            byteBuf.readBytes(this.body);
        }
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
