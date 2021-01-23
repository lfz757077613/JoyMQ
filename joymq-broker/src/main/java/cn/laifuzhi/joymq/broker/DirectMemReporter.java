package cn.laifuzhi.joymq.broker;

import cn.laifuzhi.joymq.broker.config.BrokerDynamicConf;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DirectMemReporter {
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    @Resource
    private BrokerDynamicConf brokerConf;
    private AtomicLong directMem;

    @PostConstruct
    private void init() throws IllegalAccessException {
        Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
        Objects.requireNonNull(field).setAccessible(true);
        this.directMem = (AtomicLong) field.get(PlatformDependent.class);
        log.info("DirectMemReporter init");
        report();
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        scheduledExecutor.shutdown();
        while (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("DirectMemReporter await ...");
        }
        log.info("DirectMemReporter shutdown");
    }

    private void report() {
        log.info("server direct memory size:{}b, max:{}", directMem.get(), PlatformDependent.maxDirectMemory());
        scheduledExecutor.schedule(this::report, brokerConf.getConfigBean().getDirectMemReportPeriod(), TimeUnit.MILLISECONDS);
    }

}
