package online.zust.qcqcqc.utils.manager;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.config.LimiterConfig;
import online.zust.qcqcqc.utils.entity.Limiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qcqcqc
 */
@Component
@ConditionalOnMissingBean(LimiterManager.class)
public class BaseMapLimitManager implements LimiterManager {

    private LimiterConfig limiterConfig;

    private static final ConcurrentHashMap<String, Info> STATUS_MAP = new ConcurrentHashMap<>();

    @Autowired
    public void setLimiterConfig(LimiterConfig limiterConfig) {
        this.limiterConfig = limiterConfig;
    }

    private static class Info {
        private int count;
        private long time;

        public Info(int count, long time) {
            this.count = count;
            this.time = time;
        }
    }

    /**
     * 尝试访问
     *
     * @param limiter 限制器
     * @return 是否可以访问
     */
    @Override
    public boolean tryAccess(Limiter limiter) {
        int limitNum = limiter.getLimitNum();
        int seconds = limiter.getSeconds();
        String key = limiter.getKey();
        if (limiter.isLimitByUser()) {
            key = limiterConfig.getUserKey() + "-" + key;
        }
        return tryAlterStatus(key, limitNum, seconds);
    }

    private boolean tryAlterStatus(String key, int limitNum, int seconds) {
        // 从map中获取key对应的信息
        Info info = STATUS_MAP.get(key);

        // 如果key不存在，初始化key的信息
        if (info == null) {
            STATUS_MAP.put(key, new Info(1, System.currentTimeMillis()));
            return true;
        } else {
            // 如果key存在，判断是否超过限制
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - info.time;

            if (elapsedTime > seconds * 1000L) {
                // 如果超过限制时间，重置key的信息
                info.count = 1;
                info.time = currentTime;
                return true;
            } else {
                // 如果没有超过限制时间，判断是否超过限制次数
                if (info.count < limitNum) {
                    // 如果没有超过限制次数，更新key的信息
                    info.count++;
                    return true;
                } else {
                    // 如果超过限制次数，返回false
                    return false;
                }
            }
        }
    }
}
