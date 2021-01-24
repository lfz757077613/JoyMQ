package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Ping extends BaseInfoReq {

    public Ping(String group) {
        super(DataTypeEnum.PING, group);
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.PING;
    }
}
