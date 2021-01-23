package cn.laifuzhi.joymq.broker.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigBean {
    private int channelIdleTimeout;
    private int directMemReportPeriod;
}
