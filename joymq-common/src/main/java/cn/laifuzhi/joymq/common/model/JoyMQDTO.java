package cn.laifuzhi.joymq.common.model;

import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import io.netty.buffer.ByteBuf;

public interface JoyMQDTO {
    JoyMQDTO decode(ByteBuf byteBuf);

    ByteBuf encode(ByteBuf byteBuf);

    DataTypeEnum dataType();
}
