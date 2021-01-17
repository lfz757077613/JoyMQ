package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Ping extends BaseInfo {

    public Ping(String from, String group) {
        super(DataTypeEnum.PING, from, group);
    }

    @Override
    public Ping decode(ByteBuf byteBuf) {
        super.decode(byteBuf);
        return this;
    }

    @Override
    public ByteBuf encode(ByteBuf byteBuf) {
        return super.encode(byteBuf);
    }

    @Override
    public DataTypeEnum dataType() {
        return DataTypeEnum.PING;
    }
}
