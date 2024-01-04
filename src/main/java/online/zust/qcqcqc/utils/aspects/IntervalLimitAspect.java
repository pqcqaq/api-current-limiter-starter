package online.zust.qcqcqc.utils.aspects;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.annotation.IntervalLimit;
import online.zust.qcqcqc.utils.config.condition.LimitAspectCondition;
import online.zust.qcqcqc.utils.exception.ApiCurrentLimitException;
import online.zust.qcqcqc.utils.exception.ErrorTryAccessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author qcqcqc
 * 使用CGLIB代理
 */
@Aspect
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Conditional(LimitAspectCondition.class)
@Order(2)
public class IntervalLimitAspect {
    private static final Logger log = LoggerFactory.getLogger(IntervalLimitAspect.class);

    private LimiterManager limiterManager;

    @Autowired
    public void setLimiterManager(LimiterManager limiterManager) {
        this.limiterManager = limiterManager;
    }

    @Pointcut("@annotation(online.zust.qcqcqc.utils.annotation.IntervalLimit)")
    private void check() {
    }

    @Before("check()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        IntervalLimit limit = method.getAnnotation(IntervalLimit.class);
        if (limit != null) {

            String key = limit.key().trim();
            if (key.isEmpty()) {
                key = method.getName();
            }
            // 检查间隔时间是否足够，如果不足够则抛出异常

            boolean b;
            try {
                b = limiterManager.checkInterval(limit.limitByUser(), key, limit.interval());
            } catch (Exception e) {
                log.error("限流器：{}，发生异常：{}", limiterManager.getClass().getSimpleName(), e.getMessage());
                throw new ErrorTryAccessException(e.getMessage());
            }
            if (!b) {
                log.warn("接口：{}，已被限流  key：{}，访问间隔小于:{}，限流类型：{}",
                        method.getName(), key, limit.interval(),
                        limit.limitByUser() ? "用户限流" : "全局限流");
                throw new ApiCurrentLimitException(limit.msg());
            }
        }
    }
}
