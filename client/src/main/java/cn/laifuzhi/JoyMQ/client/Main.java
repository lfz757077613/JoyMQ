package cn.laifuzhi.JoyMQ.client;

import cn.laifuzhi.RocketHttp.RocketClient;
import cn.laifuzhi.RocketHttp.model.RocketConfig;
import io.netty.channel.kqueue.KQueue;

public class Main {
    public static void main(String[] args) {
        RocketClient rocketClient = new RocketClient(RocketConfig.defaultConfig());
        KQueue.isAvailable();
        System.out.println();
    }
}
