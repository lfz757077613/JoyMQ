package cn.laifuzhi.joymq.broker.handler;

import cn.laifuzhi.joymq.broker.config.BrokerDynamicConf;
import cn.laifuzhi.joymq.common.model.BaseInfoReq;
import cn.laifuzhi.joymq.common.model.SystemResp;
import cn.laifuzhi.joymq.common.model.enums.DataTypeEnum;
import cn.laifuzhi.joymq.common.model.enums.RespTypeEnum;
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
    private BrokerDynamicConf brokerConf;
    @Resource
    private Map<String, SimpleChannelInboundHandler<? extends BaseInfoReq>> handlerMap;

    private ThreadPoolExecutor executor;
    private Map<DataTypeEnum, String> Msg2HandlerMap;

    @PostConstruct
    private void init() {
        this.executor = new ThreadPoolExecutor(
                200,
                200,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000000),
                new CustomizableThreadFactory("BrokerHandler")
        );
        this.Msg2HandlerMap = Maps.newHashMap();
        this.Msg2HandlerMap.put(DataTypeEnum.PING, PingHandler.class.getSimpleName());
        this.Msg2HandlerMap.put(DataTypeEnum.SEND_MSG_REQ, SendMsgHandler.class.getSimpleName());
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        this.executor.shutdown();
        while (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("BrokerHandler await ...");
        }
        log.info("BrokerHandler shutdown");
    }

    private SimpleChannelInboundHandler<? extends BaseInfoReq> getHandlerByMsgEnum(DataTypeEnum dataTypeEnum) {
        String handlerSimpleName = this.Msg2HandlerMap.get(dataTypeEnum);
        if (StringUtils.isBlank(handlerSimpleName)) {
            return null;
        }
        return this.handlerMap.get(handlerSimpleName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseInfoReq req) throws Exception {
        try {
            this.executor.submit(() -> {
                try {
                    SimpleChannelInboundHandler<? extends BaseInfoReq> handler = getHandlerByMsgEnum(req.dataType());
                    if (handler == null) {
                        log.error("broker not support dataType:{} reqFrom:{}", req.dataType(), req.getReqFrom());
                        new SystemResp(req.getDataId(), RespTypeEnum.DATA_TYPE_NOT_SUPPORT).writeResponse(ctx);
                        return;
                    }
                    handler.channelRead(ctx, req);
                } catch (Exception e) {
                    ctx.fireExceptionCaught(e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.error("broker busy reqFrom:{} dataId:{}", req.getReqFrom(), req.getDataId());
            new SystemResp(req.getDataId(), RespTypeEnum.BROKER_BUSY).writeResponse(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught remoteAddress:{}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
