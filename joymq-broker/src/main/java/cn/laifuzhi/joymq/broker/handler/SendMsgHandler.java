package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.broker.config.DynamicConfContainer;
import cn.laifuzhi.joymq.broker.config.DynamicConfig;
import cn.laifuzhi.joymq.broker.store.MsgService;
import cn.laifuzhi.joymq.common.model.SendMsgReq;
import cn.laifuzhi.joymq.common.model.SendMsgResp;
import cn.laifuzhi.joymq.common.model.enums.FlushTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
import cn.laifuzhi.joymq.common.utils.ChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Sharable
@Component("SendMsgHandler")
public class SendMsgHandler extends SimpleChannelInboundHandler<SendMsgReq> {
    @Resource
    private MsgService msgService;
    @Resource
    private DynamicConfContainer dynamicConfContainer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SendMsgReq sendMsgReq) throws Exception {
        try {
            DynamicConfig dynamicConfig = dynamicConfContainer.getDynamicConfig();
            if (sendMsgReq.getGroup() == null || StringUtils.length(sendMsgReq.getGroup()) > dynamicConfig.getStringMaxLength()
                    || sendMsgReq.getReqFrom() == null || StringUtils.length(sendMsgReq.getReqFrom()) > dynamicConfig.getStringMaxLength()
                    || sendMsgReq.getTopic() == null || StringUtils.length(sendMsgReq.getTopic()) > dynamicConfig.getStringMaxLength()
                    || sendMsgReq.getBody() == null || sendMsgReq.getBody().readableBytes() > dynamicConfig.getMsgBodyMaxBytes()
                    || !FlushTypeEnum.contains(sendMsgReq.getFlushType())) {
                ChannelUtil.writeResponse(ctx, new SendMsgResp(sendMsgReq, RespTypeEnum.PARAM_ERROR));
                return;
            }

            ByteBuf origin = sendMsgReq.getOrigin();
            origin.release();
            ChannelUtil.writeResponse(ctx, new SendMsgResp(sendMsgReq, RespTypeEnum.OK));
        } catch (Exception e) {
            log.error("SendMsgHandler error remoteAddress:{}", ctx.channel().remoteAddress(), e);
            ChannelUtil.writeResponse(ctx, new SendMsgResp(sendMsgReq, RespTypeEnum.UNEXPECTED_ERROR));
        }
    }
}
