package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.Ping;
import cn.laifuzhi.joymq.common.model.Pong;
import cn.laifuzhi.joymq.common.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("PingHandler")
public class PingHandler extends SimpleChannelInboundHandler<Ping> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping ping) throws Exception {
        Channel channel = ctx.channel();
        // 将第一次ping请求的reqFrom设置为channel的from属性
        ChannelUtil.setFromIfAbsent(channel, ping.getReqFrom());
        ChannelUtil.writeResponse(ctx, new Pong(ping));
    }
}
