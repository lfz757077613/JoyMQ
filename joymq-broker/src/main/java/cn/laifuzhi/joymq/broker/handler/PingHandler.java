package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.Ping;
import cn.laifuzhi.joymq.common.model.Pong;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("PingHandler")
public class PingHandler extends SimpleChannelInboundHandler<Ping> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping ping) throws Exception {
        try {
            Pong pong = new Pong("", ping.getDataId(), "1");
            ctx.writeAndFlush(pong).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } catch (Exception e) {
            log.error("PingHandler error remoteAddress:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
