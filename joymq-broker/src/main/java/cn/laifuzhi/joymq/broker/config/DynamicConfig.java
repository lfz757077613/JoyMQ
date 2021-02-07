package cn.laifuzhi.joymq.broker.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DynamicConfig {
    private int directMemReportPeriod;
    private int stringMaxLength;
    private int msgBodyMaxBytes;
}
