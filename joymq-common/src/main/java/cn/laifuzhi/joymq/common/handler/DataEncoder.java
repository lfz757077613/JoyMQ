package cn.laifuzhi.joymq.common.handler;

import cn.laifuzhi.joymq.common.model.JoyMQModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class DataEncoder extends MessageToByteEncoder<JoyMQModel> {
    public static final DataEncoder INSTANCE = new DataEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, JoyMQModel msg, ByteBuf out) throws Exception {
        try {
            out.writeInt(DataDecoder.MAGIC_NUMBER);
            int lengthWriterIndex = out.writerIndex();
            out.writerIndex(lengthWriterIndex + Integer.BYTES);
            out = msg.encode(out);
            out.setInt(lengthWriterIndex, out.writerIndex() - Integer.BYTES - Integer.BYTES);
        } catch (Exception e) {
            log.error("encode error remoteAddress:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
