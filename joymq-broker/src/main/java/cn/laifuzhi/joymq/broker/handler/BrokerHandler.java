package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.broker.config.BrokerDynamicConf;
import cn.laifuzhi.joymq.common.model.JoyMQModel;
import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Sharable
@Component
public class BrokerHandler extends SimpleChannelInboundHandler<JoyMQModel> {
    @Resource
    private BrokerDynamicConf brokerConf;
    @Resource
    private Map<String, SimpleChannelInboundHandler<? extends JoyMQModel>> handlerMap;

    private ThreadPoolExecutor executor;
    private Map<DataTypeEnum, String> Msg2HandlerMap;

    @PostConstruct
    private void init() {
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 10,
                Runtime.getRuntime().availableProcessors() * 10,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100),
                new CustomizableThreadFactory("BrokerHandler")
        );
        Msg2HandlerMap = Maps.newHashMap();
        Msg2HandlerMap.put(DataTypeEnum.PING, PingHandler.class.getSimpleName());
        Msg2HandlerMap.put(DataTypeEnum.SEND_MSG_REQ, SendMsgHandler.class.getSimpleName());
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        executor.shutdown();
        while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("BrokerHandler await ...");
        }
        log.info("BrokerHandler shutdown");
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
        executor.submit(() -> {
            try {
                SimpleChannelInboundHandler<? extends JoyMQModel> handler = getHandlerByMsgEnum(joyMQModel.dataType());
                if (handler == null) {
                    log.error("broker not support dataType:{} remoteAddress:{}", ctx.channel().remoteAddress(), joyMQModel.dataType());
                    return;
                }
                handler.channelRead(ctx, joyMQModel);
            } catch (Exception e) {
                ctx.fireExceptionCaught(e);
            }
        });
    }
}
