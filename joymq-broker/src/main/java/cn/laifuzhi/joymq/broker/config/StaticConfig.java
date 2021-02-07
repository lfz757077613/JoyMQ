package cn.laifuzhi.joymq.broker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "broker")
public class StaticConfig {
    private int port;
    private int backlog;
    private int maxDecodeBytes;
    private String dynamicConfFilename;
    private int confReloadPeriod;
    private int lowWaterMark;
    private int highWaterMark;
    private int channelIdleTimeout;
    private int writeGlobalLimit;
    private int readGlobalLimit;
    private int writeChannelLimit;
    private int readChannelLimit;
    private int brokerHandlerThreads;
    private int brokerHandlerQueueSize;
    private String msgLogStoreDirInHome;
    private String brokerRunningFile;
}
