package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Pong extends BaseInfoResp {

    public Pong(int dataId) {
        super(DataTypeEnum.PONG, dataId, RespTypeEnum.OK);
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.PONG;
    }
}
