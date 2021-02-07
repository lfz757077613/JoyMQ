package cn.laifuzhi.joymq.common.utils;

import cn.laifuzhi.joymq.common.model.BaseInfoResp;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelUtil {
    private static final AttributeKey<String> FROM = AttributeKey.newInstance("FROM");

    public static void setFromIfAbsent(Channel channel, String from) {
        channel.attr(FROM).setIfAbsent(from);
    }

    public static String getFrom(Channel channel) {
        return channel.attr(FROM).get();
    }

    /**
     * addListener时可能eventloop已经关闭了，所以会打异常堆栈
     */
    public static void writeResponse(ChannelHandlerContext ctx, BaseInfoResp resp) {
        Channel channel = ctx.channel();
        if (!channel.isActive() || !channel.isWritable()) {
            log.info("channel not active or not writable active:{} writable:{} respType:{} reqFrom:{} dataId:{} remoteAddress:{}",
                    channel.isActive(), channel.isWritable(), resp.getRespType(), resp.getReqFrom(), resp.getDataId(), channel.remoteAddress());
            return;
        }
        channel.writeAndFlush(resp).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("writeResponse error respType:{} reqFrom:{} dataId:{} remoteAddress:{}",
                        resp.getRespType(), resp.getReqFrom(), resp.getDataId(), channel.remoteAddress(), future.cause());
                channel.close();
            }
        });
    }
}
