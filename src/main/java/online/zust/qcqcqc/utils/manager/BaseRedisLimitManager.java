package online.zust.qcqcqc.utils.manager;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.config.LimiterConfig;
import online.zust.qcqcqc.utils.entity.Limiter;
import online.zust.qcqcqc.utils.exception.CannotLoadLuaScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author pqcmm
 */
@Component
@Primary
@ConditionalOnProperty(value = "limiter.type", havingValue = "redis")
public class BaseRedisLimitManager implements LimiterManager {

    private static final Logger log = LoggerFactory.getLogger(BaseRedisLimitManager.class);

    private RedisTemplate<String, Long> redisTemplate;

    private LimiterConfig limiterConfig;

    private static byte[] TRY_ALTER_STATUS_SCRIPT_BYTE;
    private static byte[] CHECK_INTERVAL_SCRIPT_BYTE;
    private static byte[] CHECK_CONCURRENT_SCRIPT_BYTE;

    @Autowired
    public void setLimiterConfig(LimiterConfig limiterConfig) {
        this.limiterConfig = limiterConfig;
    }

    @Resource
    public void setRedisTemplate(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 读取lua脚本
        ClassPathResource tryAlterStatusResource = new ClassPathResource("scripts/tryAlterStatus.lua");
        ClassPathResource checkIntervalResource = new ClassPathResource("scripts/checkInterval.lua");
        ClassPathResource checkConcurrentResource = new ClassPathResource("scripts/checkConcurrent.lua");
        try {
            TRY_ALTER_STATUS_SCRIPT_BYTE = tryAlterStatusResource.getInputStream().readAllBytes();
            CHECK_INTERVAL_SCRIPT_BYTE = checkIntervalResource.getInputStream().readAllBytes();
            CHECK_CONCURRENT_SCRIPT_BYTE = checkConcurrentResource.getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("读取lua脚本失败：{}", e.getMessage());
            throw new CannotLoadLuaScriptException("读取lua脚本失败：" + e.getMessage());
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
            key = limiterConfig.getUserKey() + "-" + key + "-access";
        }
        return tryAlterStatus(key, limitNum, seconds);
    }

    private boolean tryAlterStatus(String key, int limitNum, int seconds) {
        // 获取当前时间，精确到纳秒
        long currentTimeNanos = System.nanoTime();

        // Lua脚本
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> connection.eval(
                TRY_ALTER_STATUS_SCRIPT_BYTE,
                ReturnType.INTEGER,
                1,
                redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(String.valueOf(limitNum)),
                redisTemplate.getStringSerializer().serialize(String.valueOf(seconds)),
                redisTemplate.getStringSerializer().serialize(String.valueOf(currentTimeNanos))
        ));


        // 返回结果
        return result != null && result.equals(1L);
    }

    /**
     * 检查两次请求的间隔时间
     *
     * @param limitByUser 是否根据用户限流
     * @param key         限流key
     * @param interval    间隔时间
     * @return 是否可以访问
     */
    @Override
    public boolean checkInterval(boolean limitByUser, String key, long interval) {
        int maxLog = 21;
        // 生成key
        if (limitByUser) {
            key = limiterConfig.getUserKey() + "-" + key + "-interval";
        }
        return tryAlterStatus(key, interval, maxLog);
    }

    private boolean tryAlterStatus(String key, long interval, int maxLog) {
        // 若key存在，检查上一次请求和这一次请求的间隔时间
        //  如果间隔时间小于指定的间隔时间，返回false，否则返回true并添加当前时间到有序集合中
        // 若不存在，直接返回true，并添加当前时间到有序集合中
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> connection.eval(
                CHECK_INTERVAL_SCRIPT_BYTE,
                ReturnType.INTEGER,
                1,
                redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(String.valueOf(interval)),
                redisTemplate.getStringSerializer().serialize(String.valueOf(System.nanoTime())),
                redisTemplate.getStringSerializer().serialize(String.valueOf(maxLog))
        ));
        return result != null && result.equals(1L);
    }

    @Override
    public boolean checkConcurrent(boolean limitByUser, String key, int limitNum, boolean set) {
        if (limitByUser) {
            key = limiterConfig.getUserKey() + "-" + key + "-concurrent";
        }
        return tryAlterStatus(key, limitNum, set);
    }

    private boolean tryAlterStatus(String key, int limitNum, boolean set) {
        long currentTime = System.nanoTime();
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> connection.eval(
                CHECK_CONCURRENT_SCRIPT_BYTE,
                ReturnType.INTEGER,
                1,
                redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(String.valueOf(limitNum)),
                redisTemplate.getStringSerializer().serialize(String.valueOf(set)),
                redisTemplate.getStringSerializer().serialize(String.valueOf(currentTime))
        ));
        return result != null && result.equals(1L);
    }
}
