package online.zust.qcqcqc.utils.manager;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.config.LimiterConfig;
import online.zust.qcqcqc.utils.entity.Limiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author pqcmm
 */
@Component
@Primary
@ConditionalOnProperty(value = "limiter.type", havingValue = "redis")
public class BaseRedisLimitManager implements LimiterManager {

    private RedisTemplate<String, Integer> redisTemplate;
    private LimiterConfig limiterConfig;

    @Autowired
    public void setLimiterConfig(LimiterConfig limiterConfig) {
        this.limiterConfig = limiterConfig;
    }

    @Resource
    public void setRedisTemplate(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

    public boolean tryAlterStatus(String key, int limitNum, int seconds) {
        // lua脚本
        String script = """
                local key = ${key};
                local limitNum = tonumber(${limitNum});
                local seconds = tonumber(${seconds});
                local currentNum = redis.call('incr', key);
                if currentNum == 1 then
                    redis.call('expire', key, seconds);
                end;
                if currentNum > limitNum then
                    return 0;
                end;
                return 1;
                """;
        // 执行lua脚本
        Long result = redisTemplate.execute(new DefaultRedisScript<>(
                script.replace("${key}", "'" + key + "'")
                        .replace("${limitNum}", String.valueOf(limitNum))
                        .replace("${seconds}", String.valueOf(seconds)),
                        Long.class),Collections.emptyList());
        // 返回结果
        return result != null && result == 1;
    }


}
