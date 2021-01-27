package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.Ping;
import cn.laifuzhi.joymq.common.model.Pong;
import com.alibaba.fastjson.JSON;
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
        try {
            if (channel.isWritable()) {
                log.error("PingHandler not writable ping:{}", JSON.toJSONString(ping));
                return;
            }
            new Pong(ping).writeResponse(ctx);
        } catch (Exception e) {
            log.error("PingHandler error remoteAddress:{}", channel.remoteAddress(), e);
        }
    }
}
