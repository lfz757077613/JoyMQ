package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.SendMsgReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Sharable
@Component("SendMsgHandler")
public class SendMsgHandler extends SimpleChannelInboundHandler<SendMsgReq> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SendMsgReq sendMsgReq) throws Exception {
        try {
            ByteBuf body = sendMsgReq.getBody();
            body.release();
        } catch (Exception e) {
            log.error("SendMsgHandler error remoteAddress:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
