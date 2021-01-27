package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.SendMsgReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Sharable
@Component("SendMsgHandler")
public class SendMsgHandler extends SimpleChannelInboundHandler<SendMsgReq> {
    private AtomicInteger count = new AtomicInteger();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SendMsgReq sendMsgReq) throws Exception {
        try {
            ByteBuf origin = sendMsgReq.getOrigin();
            origin.release();
            if (count.incrementAndGet() == 1 || count.get() % 500000 == 0) {
                log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
//            ctx.executor().execute(origin::release);
        } catch (Exception e) {
            log.error("SendMsgHandler error remoteAddress:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
