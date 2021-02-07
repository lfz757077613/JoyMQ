package cn.laifuzhi.joymq.broker.store;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MsgPO {
    private String topic;
    private long storeTime;
    private long crc32;
    private ByteBuf body;

    public static MsgPO decode(ByteBuffer byteBuffer) {
        return new MsgPO();
    }
}
