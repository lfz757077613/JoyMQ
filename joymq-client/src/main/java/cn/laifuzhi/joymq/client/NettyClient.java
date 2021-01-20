package cn.laifuzhi.joymq.client;

import cn.laifuzhi.joymq.common.handler.DataDecoder;
import cn.laifuzhi.joymq.common.handler.DataEncoder;
import cn.laifuzhi.joymq.common.model.Ping;
import cn.laifuzhi.joymq.common.model.SendMsgReq;
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
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger atomicInteger = new AtomicInteger();
//        channel.writeAndFlush(new Ping("1","")).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        for (; ; ) {
//            TimeUnit.MILLISECONDS.sleep(10);
            if (!channel.isWritable()) {
                continue;
            }
            channel.writeAndFlush(new SendMsgReq("", "2", "", "123".getBytes())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            System.out.println(atomicInteger.incrementAndGet());
        }

//                TimeUnit.HOURS.sleep(1);
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        ByteBuffer allocate2 = ByteBuffer.allocate(16);
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16);
//        ((DirectBuffer)byteBuffer).cleaner().clean();
//        byteBuffer.putInt(11);
//        final RandomAccessFile rawFile = new RandomAccessFile("/Users/lfz/Desktop/test.txt", "rw");
//        FileChannel fileChannel = rawFile.getChannel();
//        MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
//        fileChannel.write(ByteBuffer.wrap("6".getBytes()));
//        fileChannel.force(false);
//        byte[] bytes1 = new byte[3];
//        map.get(bytes1, 0, 3);
//        System.out.println(new String(bytes1));
//        map.put("6".getBytes(), 0, "7".getBytes().length);
//        map.force();
//        ByteBuffer allocate1 = ByteBuffer.allocate(10);
//        byte[] bytes = "123".getBytes();
//        ByteBuffer allocate = ByteBuffer.wrap(bytes);
//        System.out.println(fileChannel.position());
//        System.out.println(fileChannel.size());
//        fileChannel.write(allocate);
//        System.out.println(fileChannel.position());
//        System.out.println(fileChannel.size());
//
//        fileChannel.force(false);
//        fileChannel.close();
//    }
}
