package online.zust.qcqcqc.utils;

import online.zust.qcqcqc.utils.entity.Limiter;

/**
 * @author pqcmm
 */
public interface LimiterManager {
    /**
     * 尝试访问
     * @param limiter 限制器
     * @return 是否可以访问
     */
    boolean tryAccess(Limiter limiter);

    /**
     * 检查两次请求的间隔时间
     * @param limiter 限制器
     * @param key 限流key
     * @return 是否可以访问
     */
    boolean checkInterval(Limiter limiter, String key, long interval);
}
