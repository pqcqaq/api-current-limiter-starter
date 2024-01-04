package online.zust.qcqcqc.utils.manager;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.config.LimiterConfig;
import online.zust.qcqcqc.utils.entity.Limiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author qcqcqc
 */
@Component
@ConditionalOnMissingBean(LimiterManager.class)
public class BaseMapLimitManager implements LimiterManager {

    private LimiterConfig limiterConfig;

    private static final ConcurrentHashMap<String, List<Long>> STATUS_MAP = new ConcurrentHashMap<>();

    @Autowired
    public void setLimiterConfig(LimiterConfig limiterConfig) {
        this.limiterConfig = limiterConfig;
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
            key = limiterConfig.getUserKey() + "-" + key + "-access";
        }
        return tryAlterStatus(key, limitNum, seconds);
    }

    @Override
    public boolean checkInterval(boolean limitByUser, String key, long interval) {
        // 生成key
        if (limitByUser) {
            key = limiterConfig.getUserKey() + "-" + key + "-interval";
        }
        return tryAlterStatus(key, interval);
    }

    private static boolean tryAlterStatus(String key, long interval) {
        // 若key存在，检查上一次请求和这一次请求的间隔时间
        List<Long> infos = STATUS_MAP.get(key);
        if (infos != null && !infos.isEmpty()) {
            long lastTime = infos.get(infos.size() - 1);
            long currentTime = System.nanoTime();
            // 如果间隔时间小于指定的间隔时间，返回false
            if (currentTime - lastTime < interval * 1000L * 1000 ) {
                return false;
            }else{
                // 如果间隔时间大于指定的间隔时间, 允许访问，并更新key的信息
                infos.add(currentTime);
                return true;
            }
        } else {
            // 如果key不存在，初始化key的信息
            STATUS_MAP.putIfAbsent(key, new CopyOnWriteArrayList<>() {
                @Serial
                private static final long serialVersionUID = -7011652858814940405L;
                {
                    add(System.nanoTime());
                }
            });
            return true;
        }
    }

    private static boolean tryAlterStatus(String key, int limitNum, int seconds) {
        // 从map中获取key对应的信息
        List<Long> infos = STATUS_MAP.get(key);

        // 如果key不存在，初始化key的信息
        if (infos == null) {
            STATUS_MAP.putIfAbsent(key, new CopyOnWriteArrayList<>() {
                @Serial
                private static final long serialVersionUID = -7011652858814940405L;

                {
                    add(System.nanoTime());
                }
            });
            return true;
        } else {
            // key存在，判断指定时间内是否超过限制次数
            long currentTime = System.nanoTime();
            long startTime = currentTime - seconds * 1000L * 1000 * 1000;
            // 删除指定时间之前的数据
            while (!infos.isEmpty() && infos.get(0) < startTime) {
                infos.remove(0);
            }
            // 判断是否超过限制次数
            if (infos.size() < limitNum) {
                // 如果没有超过限制次数，更新key的信息
                infos.add(currentTime);
                return true;
            } else {
                // 如果超过限制次数，返回false
                return false;
            }
        }
    }
}
