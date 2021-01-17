package cn.laifuzhi.joymq.client;

import cn.laifuzhi.joymq.common.handler.DataDecoder;
import cn.laifuzhi.joymq.common.handler.DataEncoder;
import cn.laifuzhi.joymq.common.model.Ping;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient {

    private static final int lowWaterMark = 2 * 102 ;
    private static final int highWaterMark = 4 * 102 ;
    private static final int channelIdleTimeout = 600;

    private Bootstrap bootstrap;

    private int brokerPort;

//    private ClientHandlerWrapper brokerHandlerWrapper;

    public NettyClient() {
        EventLoopGroup eventLoopGroup = null;
        Class<? extends Channel> channelClass = null;
//        if (Epoll.isAvailable()) {
//            eventLoopGroup = new EpollEventLoopGroup();
//            channelClass = EpollSocketChannel.class;
//        }
//        if (KQueue.isAvailable()) {
//            eventLoopGroup = new KQueueEventLoopGroup();
//            channelClass = KQueueSocketChannel.class;
//        }
        eventLoopGroup = eventLoopGroup != null ? eventLoopGroup : new NioEventLoopGroup();
        channelClass = channelClass != null ? channelClass : NioSocketChannel.class;
        //只监听一个端口，bossGroup只设置一个线程就可以
        bootstrap = new Bootstrap().group(eventLoopGroup)
                .channel(channelClass)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(lowWaterMark, highWaterMark))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 60s收不到client心跳认为连接假死(也就是收不到ping)，关闭channel
                        p.addLast(new ConnectHandler(channelIdleTimeout, 0, 0));
                        p.addLast(new DataDecoder());
                        p.addLast(DataEncoder.INSTANCE);
//                        p.addLast(brokerHandlerWrapper);
                    }
                });
    }

    public Channel connect() {
        return bootstrap.connect("127.0.0.1", 6792).syncUninterruptibly().channel();
    }

    public static void main(String[] args) throws InterruptedException {
        GlobalEventExecutor eventExecutors = GlobalEventExecutor.INSTANCE;
        Channel channel = new NettyClient().connect();
        channel.writeAndFlush(new Ping("1","")).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//        for (; ; ) {
//            TimeUnit.MILLISECONDS.sleep(10);
//            if (!channel.isWritable()) {
//                continue;
//            }
//            channel.writeAndFlush(new Ping("赖福智1111111111111111111111111111111111111"));
//        }

                TimeUnit.HOURS.sleep(1);
    }
}
