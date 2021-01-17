package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.common.model.JoyMQModel;
import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Sharable
@Component
public class BrokerHandlerWrapper extends SimpleChannelInboundHandler<JoyMQModel> {
    @Resource
    private Map<String, SimpleChannelInboundHandler<? extends JoyMQModel>> handlerMap;

    private Map<DataTypeEnum, String> Msg2HandlerMap;

    public BrokerHandlerWrapper() {
        Msg2HandlerMap = Maps.newHashMap();
        Msg2HandlerMap.put(DataTypeEnum.PING, PingHandler.class.getSimpleName());
        Msg2HandlerMap.put(DataTypeEnum.SEND_MSG_REQ, SendMsgHandler.class.getSimpleName());
    }

    private SimpleChannelInboundHandler<? extends JoyMQModel> getHandlerByMsgEnum(DataTypeEnum dataTypeEnum) {
        String handlerSimpleName = Msg2HandlerMap.get(dataTypeEnum);
        if (StringUtils.isBlank(handlerSimpleName)) {
            return null;
        }
        return handlerMap.get(handlerSimpleName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JoyMQModel joyMQModel) throws Exception {
        SimpleChannelInboundHandler<? extends JoyMQModel> handler = getHandlerByMsgEnum(joyMQModel.dataType());
        if (handler == null) {
            log.error("broker not support dataType:{} remoteAddress:{}", ctx.channel().remoteAddress(), joyMQModel.dataType());
            return;
        }
        handler.channelRead(ctx, joyMQModel);
    }
}
