package cn.laifuzhi.joymq.broker;

import cn.laifuzhi.joymq.broker.config.BrokerDynamicConf;
import cn.laifuzhi.joymq.broker.handler.BrokerHandler;
import cn.laifuzhi.joymq.broker.handler.ConnectHandler;
import cn.laifuzhi.joymq.common.handler.DataDecoder;
import cn.laifuzhi.joymq.common.handler.DataEncoder;
import cn.laifuzhi.joymq.common.utils.UtilAll;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Slf4j
@Component
public class NettyServer {
    @Value("${broker.port}")
    private int brokerPort;
    @Value("${broker.backlog}")
    private int backlog;
    @Resource
    private BrokerHandler brokerHandler;
    @Resource
    private BrokerDynamicConf brokerConf;

    private ServerBootstrap serverBootstrap;

    @PostConstruct
    private void init() {
        systemCheck();
        // 只监听一个端口，bossGroup只设置一个线程就可以
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        // linux优先使用native能力，mac的kqueue是UnstableApi所以不使用
        if (Epoll.isAvailable()) {
            bossEventLoopGroup = new EpollEventLoopGroup(1);
            workerEventLoopGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        }
        this.serverBootstrap = new ServerBootstrap().group(bossEventLoopGroup, workerEventLoopGroup)
                .channel(channelClass)
                .localAddress(brokerPort)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 60s收不到client心跳认为连接假死(也就是收不到ping)，关闭channel
                        p.addLast(new ConnectHandler(brokerConf.getConfigBean().getChannelIdleTimeout(), 0, 0));
                        p.addLast(new DataDecoder());
                        p.addLast(DataEncoder.INSTANCE);
                        p.addLast(brokerHandler);
                    }
                });
        this.serverBootstrap.bind().syncUninterruptibly();
        log.info("JoyMQ broker start on port:{}", brokerPort);
    }

    @PreDestroy
    private void destroy() {
        this.serverBootstrap.config().group().shutdownGracefully().awaitUninterruptibly();
        this.serverBootstrap.config().childGroup().shutdownGracefully().awaitUninterruptibly();
        log.info("JoyMQ broker shutdown");
    }

    private void systemCheck() {
        Preconditions.checkArgument(
                UtilAll.getInnerIp() != null
                        && UtilAll.getPid() > 0
                        && StringUtils.length(UtilAll.getFrom()) <= 64
        );
    }
}
