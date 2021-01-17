package cn.laifuzhi.joymq.broker;

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DirectMemReporter {
    private static final String THREAD_NAME = "directMem";
    private ScheduledExecutorService executor = MoreExecutors.getExitingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory(THREAD_NAME)),
            10,
            TimeUnit.SECONDS);

    private long periodSeconds = 10;

    @PostConstruct
    private void init() throws IllegalAccessException {
        Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
        Objects.requireNonNull(field).setAccessible(true);
        AtomicLong directMem = (AtomicLong) field.get(PlatformDependent.class);
        log.info("DirectMemReporter start");
        executor.scheduleAtFixedRate(
                () -> log.info("server direct memory size:{}b, max:{}", directMem.get(), PlatformDependent.maxDirectMemory())
                , periodSeconds
                , periodSeconds
                , TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        executor.shutdown();
        while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("DirectMemReporter await ...");
        }
    }

}
