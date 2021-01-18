package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMsgResp extends BaseInfo {
    private RespTypeEnum respType;

    public SendMsgResp(String from, int dataId, String group, RespTypeEnum respType) {
        super(DataTypeEnum.SEND_MSG_RESP, from, dataId, group);
        this.respType = respType;
    }

    @Override
    public SendMsgResp decode(ByteBuf byteBuf) {
        super.decode(byteBuf);
        byte respTypeByte = byteBuf.readByte();
        this.respType = RespTypeEnum.getByType(respTypeByte);
        if (this.respType == null) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        super.encode(byteBuf);
        return byteBuf.writeByte(this.respType.getType());
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.SEND_MSG_RESP;
    }
}
