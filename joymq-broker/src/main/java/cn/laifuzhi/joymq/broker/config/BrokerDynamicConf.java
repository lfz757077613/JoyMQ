package cn.laifuzhi.joymq.broker.config;

import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BrokerDynamicConf {
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    @Value("${broker.confFilename}")
    private String filename;
    @Value("${broker.confReloadPeriod}")
    private int reloadPeriod;
    private File file;
    private long lastModified;
    @Getter
    private volatile ConfigBean configBean;

    @PostConstruct
    private void init() {
        String jarPath = new ApplicationHome(getClass()).getDir().getAbsolutePath();
        this.file = new File(jarPath + File.separator + filename);
        log.info("BrokerConf init");
        checkAndConfigure();
        scheduledExecutor.scheduleWithFixedDelay(this::checkAndConfigure, reloadPeriod, reloadPeriod, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        scheduledExecutor.shutdown();
        while (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("BrokerConf await ...");
        }
        log.info("BrokerConf shutdown");
    }

    private void checkAndConfigure() {
        try {
            long start = System.currentTimeMillis();
            if (!this.file.exists()) {
                log.error("checkAndConfigure not exist file:{}", this.file.getAbsolutePath());
                return;
            }
            long fileModified = this.file.lastModified();
            if (fileModified > this.lastModified) {
                this.configBean = JSON.parseObject(Files.asCharSource(this.file, StandardCharsets.UTF_8).read(), ConfigBean.class);
                this.lastModified = fileModified;
                log.info("checkAndConfigure finish file:{} cost:{}", this.file.getAbsolutePath(), System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("checkAndConfigure error file:{}", this.file.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }
}
