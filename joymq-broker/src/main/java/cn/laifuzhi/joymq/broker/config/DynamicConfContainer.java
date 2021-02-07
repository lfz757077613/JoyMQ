package cn.laifuzhi.joymq.broker.config;

import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DynamicConfContainer {
    @Resource
    private StaticConfig staticConfig;

    private ScheduledExecutorService scheduledExecutor;
    private File file;
    private long lastModified;
    @Getter
    private volatile DynamicConfig dynamicConfig;

    @PostConstruct
    private void init() {
        String jarPath = new ApplicationHome(getClass()).getDir().getAbsolutePath();
        file = new File(jarPath + File.separator + staticConfig.getDynamicConfFilename());
        log.info("DynamicConfContainer init");
        checkAndConfigure();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(this::checkAndConfigure, staticConfig.getConfReloadPeriod(), staticConfig.getConfReloadPeriod(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        scheduledExecutor.shutdown();
        while (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("DynamicConfContainer await ...");
        }
        log.info("DynamicConfContainer shutdown");
    }

    private void checkAndConfigure() {
        try {
            long start = System.currentTimeMillis();
            if (!file.exists()) {
                log.error("DynamicConfContainer checkAndConfigure not exist file:{}", file.getAbsolutePath());
                return;
            }
            long fileModified = file.lastModified();
            if (fileModified > lastModified) {
                dynamicConfig = JSON.parseObject(Files.asCharSource(file, StandardCharsets.UTF_8).read(), DynamicConfig.class);
                lastModified = fileModified;
                log.info("DynamicConfContainer checkAndConfigure finish file:{} cost:{}", file.getAbsolutePath(), System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("DynamicConfContainer checkAndConfigure error file:{}", file.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }
}
