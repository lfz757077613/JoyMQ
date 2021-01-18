package cn.laifuzhi.joymq.broker.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class CommitLogService {
    // 也可以考虑用AtomicBoolean自旋
    private ReentrantLock lock = new ReentrantLock();
    public boolean saveMsg() {
        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                log.error("saveMsg tryLock fail");
                return false;
            }
            try {

            } finally {
                lock.unlock();
            }
            return true;
        } catch (Exception e) {
            log.error("saveMsg error", e);
            return false;
        }
    }
}
