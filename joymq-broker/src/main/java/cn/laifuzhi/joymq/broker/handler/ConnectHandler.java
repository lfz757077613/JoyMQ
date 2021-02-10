package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectHandler extends IdleStateHandler {
    public ConnectHandler(int readerIdleTime, int writerIdleTime, int allIdleTime) {
        super(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        Channel channel = ctx.channel();
        log.info("channelIdle remoteAddress:{} from:{}", channel.remoteAddress(), ChannelUtil.getFrom(channel));
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("channelActive remoteAddress:{} from:{}", channel.remoteAddress(), ChannelUtil.getFrom(channel));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("channelInactive remoteAddress:{} from:{}", channel.remoteAddress(), ChannelUtil.getFrom(channel));
        super.channelInactive(ctx);
    }

    // channel不可写时关闭自动读取
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("channelWritabilityChanged remoteAddress:{} from:{} writable:{}", channel.remoteAddress(), ChannelUtil.getFrom(channel), channel.isWritable());
        ctx.channel().config().setAutoRead(ctx.channel().isWritable());
        super.channelWritabilityChanged(ctx);
    }
}
