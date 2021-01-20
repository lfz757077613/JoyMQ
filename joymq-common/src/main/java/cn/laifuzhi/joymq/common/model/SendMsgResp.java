package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

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
        Optional<RespTypeEnum> respTypeOptional = RespTypeEnum.getByType(byteBuf.readByte());
        if (!respTypeOptional.isPresent()) {
            throw new IllegalArgumentException();
        }
        this.respType = respTypeOptional.get();
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
