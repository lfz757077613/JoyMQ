package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemResp extends BaseInfoResp {
    public SystemResp(int dataId, RespTypeEnum respType) {
        super(DataTypeEnum.SYSTEM_RESP, dataId, respType);
    }

    public SystemResp(BaseInfoReq req, RespTypeEnum respType) {
        super(req, DataTypeEnum.SYSTEM_RESP, respType);
    }
    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.SYSTEM_RESP;
    }
}
