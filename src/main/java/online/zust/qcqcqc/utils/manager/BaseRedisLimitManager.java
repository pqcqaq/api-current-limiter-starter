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

    @Override
    public boolean checkInterval(Limiter limiter, String key, long interval) {
        // 生成key
        if (limiter.isLimitByUser()) {
            key = limiterConfig.getUserKey() + "-" + key;
        }
        // 若key存在，检查上一次请求和这一次请求的间隔时间
        //  如果间隔时间小于指定的间隔时间，返回false，否则返回true并添加当前时间到有序集合中
        // 若不存在，直接返回true，并添加当前时间到有序集合中
        String script = """
                local key = ${key};
                local interval = tonumber(${interval});
                local currentTime = tonumber(${currentTimeNanos});

                -- 获取当前有序集合的元素数量
                local currentNum = redis.call('zcard', key);

                -- 判断是否超过限制次数
                if currentNum == 0 then
                    redis.call('zadd', key, currentTime, currentTime);
                    return 1;
                else
                    local lastTime = redis.call('zrange', key, -1, -1)[1];
                    if currentTime - lastTime < interval * 1000000 then
                        return 0;
                    else
                        redis.call('zadd', key, currentTime, currentTime);
                        return 1;
                    end;
                end;
                """;
        Long execute = redisTemplate.execute(new DefaultRedisScript<>(
                script.replace("${key}", "'" + key + "'")
                        .replace("${interval}", String.valueOf(interval))
                        .replace("${currentTimeNanos}", String.valueOf(System.nanoTime()))
                ,
                Long.class), Collections.emptyList());
        return execute != null && execute == 1;
    }

    private boolean tryAlterStatus(String key, int limitNum, int seconds) {
        // 获取当前时间，精确到纳秒
        long currentTimeNanos = System.nanoTime();

        // Lua脚本
        String script = """
                local key = ${key};
                local limitNum = tonumber(${limitNum});
                local seconds = tonumber(${seconds});
                local currentTime = tonumber(${currentTimeNanos});

                -- 删除指定时间之前的数据
                redis.call('zremrangebyscore', key, '-inf', currentTime - seconds * 1000000000);

                -- 获取当前有序集合的元素数量
                local currentNum = redis.call('zcard', key);

                -- 判断是否超过限制次数
                if currentNum < limitNum then
                    redis.call('zadd', key, currentTime, currentTime);
                    return 1;
                else
                    return 0;
                end;
                """;

        // 执行Lua脚本
        Long result = redisTemplate.execute(new DefaultRedisScript<>(
                script.replace("${key}", "'" + key + "'")
                        .replace("${limitNum}", String.valueOf(limitNum))
                        .replace("${seconds}", String.valueOf(seconds))
                        .replace("${currentTimeNanos}", String.valueOf(currentTimeNanos))
                ,
                Long.class), Collections.emptyList());

        // 返回结果
        return result != null && result == 1;
    }
}
