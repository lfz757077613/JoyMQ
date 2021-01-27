package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMsgResp extends BaseInfoResp {

    public SendMsgResp(SendMsgReq req, RespTypeEnum respType) {
        super(req, DataTypeEnum.SEND_MSG_RESP, respType);
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.SEND_MSG_RESP;
    }
}
