package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.broker.config.StaticConfig;
import cn.laifuzhi.joymq.common.model.BaseInfoReq;
import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.utils.ChannelUtil;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Sharable
@Component
public class BrokerHandler extends SimpleChannelInboundHandler<BaseInfoReq> {
    @Resource
    private StaticConfig staticConfig;
    @Resource
    private Map<String, SimpleChannelInboundHandler<? extends BaseInfoReq>> handlerMap;

    private ThreadPoolExecutor executor;
    private Map<DataTypeEnum, String> Req2HandlerMap;

    @PostConstruct
    private void init() {
        executor = new ThreadPoolExecutor(
                staticConfig.getBrokerHandlerThreads(),
                staticConfig.getBrokerHandlerThreads(),
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(staticConfig.getBrokerHandlerQueueSize()),
                new CustomizableThreadFactory("BrokerHandler")
        );
        Req2HandlerMap = Maps.newHashMap();
        Req2HandlerMap.put(DataTypeEnum.PING, PingHandler.class.getSimpleName());
        Req2HandlerMap.put(DataTypeEnum.SEND_MSG_REQ, SendMsgHandler.class.getSimpleName());
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        executor.shutdown();
        while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("BrokerHandler await ...");
        }
        log.info("BrokerHandler shutdown");
    }

    private SimpleChannelInboundHandler<? extends BaseInfoReq> getHandlerByMsgEnum(DataTypeEnum dataTypeEnum) {
        String handlerSimpleName = Req2HandlerMap.get(dataTypeEnum);
        if (StringUtils.isBlank(handlerSimpleName)) {
            return null;
        }
        return handlerMap.get(handlerSimpleName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseInfoReq req) throws Exception {
        try {
            executor.submit(() -> runHandler(ctx, req));
        } catch (RejectedExecutionException e) {
            log.info("broker busy run in netty thread directly reqFrom:{} dataId:{}", req.getReqFrom(), req.getDataId());
            runHandler(ctx, req);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught remoteAddress:{} from:{}",
                ctx.channel().remoteAddress(), ChannelUtil.getFrom(ctx.channel()), cause);
        ctx.close();
    }

    private void runHandler(ChannelHandlerContext ctx, BaseInfoReq req) {
        try {
            SimpleChannelInboundHandler<? extends BaseInfoReq> handler = getHandlerByMsgEnum(req.dataType());
            if (handler == null) {
                log.error("broker not support dataType:{} reqFrom:{}", req.dataType(), req.getReqFrom());
                return;
            }
            handler.channelRead(ctx, req);
        } catch (Exception e) {
            // 应该不会执行到这里
            ctx.fireExceptionCaught(e);
        }
    }
}
