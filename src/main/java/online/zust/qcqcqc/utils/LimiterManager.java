package online.zust.qcqcqc.utils;

import online.zust.qcqcqc.utils.entity.Limiter;

/**
 * @author pqcmm
 */
public interface LimiterManager {
    /**
     * 尝试访问
     *
     * @param limiter 限制器
     * @return 是否可以访问
     */
    boolean tryAccess(Limiter limiter);

    /**
     * 检查两次请求的间隔时间
     *
     * @param limitByUser 是否根据用户限流
     * @param key         限流key
     * @param interval    间隔时间
     * @return 是否可以访问
     */
    boolean checkInterval(boolean limitByUser, String key, long interval);

    /**
     * 检查并发数
     *
     * @param limitByUser 是否根据用户限流
     * @param key         限流key
     * @param limitNum    最大并发数
     * @param set         是否为记录请求，否则删除记录
     * @return 是否可以访问
     */
    boolean checkConcurrent(boolean limitByUser, String key, int limitNum, boolean set);
}
