package cn.laifuzhi.joymq.common.handler;

import cn.laifuzhi.joymq.common.model.JoyMQModel;
import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

// 私有协议，前四字节是魔数，接着四个字节是数据长度，接着是一个完整数据包
// 完整数据包的第一个字节是类型，然后两个字节是base数据长度，然后是base数据，最后是其他数据
@Slf4j
public class DataDecoder extends LengthFieldBasedFrameDecoder {
    public static final int MAGIC_NUMBER = 0xdeadbeaf;
    private static final int MAX_BYTES = 4 * 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 4;
    private static final int LENGTH_FIELD_LENGTH = 4;

    public DataDecoder() {
        super(MAX_BYTES, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, 0, 8);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = null;
        try {
            //不是魔数开头直接拒绝访问
            if (in.readableBytes() >= Integer.BYTES && in.getInt(in.readerIndex()) != MAGIC_NUMBER) {
                log.info("magic number wrong remoteAddress:{} number:{}",
                        ctx.channel().remoteAddress(), Integer.toHexString(in.getInt(in.readerIndex())));
                ctx.close();
                return null;
            }
            byteBuf = (ByteBuf) super.decode(ctx, in);
            if (byteBuf == null) {
                return null;
            }
            byte dataType = byteBuf.getByte(0);
            DataTypeEnum dataTypeEnum = DataTypeEnum.getByType(dataType);
            if (dataTypeEnum == null) {
                log.error("no such msg, dataType:{}", dataType);
                return null;
            }
            Constructor<? extends JoyMQModel> constructor = dataTypeEnum.getMqModelClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance().decode(byteBuf);
        } catch (Exception e) {
            log.error("decode error remoteAddress:{}", ctx.channel().remoteAddress(), e);
            return null;
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }
}
