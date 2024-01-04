package online.zust.qcqcqc.utils.aspects;

import online.zust.qcqcqc.utils.LimiterManager;
import online.zust.qcqcqc.utils.config.condition.GlobalLimitAspectCondition;
import online.zust.qcqcqc.utils.entity.Limiter;
import online.zust.qcqcqc.utils.exception.ApiCurrentLimitException;
import online.zust.qcqcqc.utils.exception.ErrorTryAccessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author qcqcqc
 */
@Aspect
@Component
@Conditional(GlobalLimitAspectCondition.class)
@Order(4)
public class GlobalLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalLimitAspect.class);

    @Autowired
    private LimiterManager limiterManager;
    @Value("${limiter.global.limit-num:100}")
    private Integer limitNum;
    @Value("${limiter.global.seconds:10}")
    private Integer seconds;
    @Value("${limiter.global.on-method:true}")
    private Boolean onMethod;

    @Before("@annotation(online.zust.qcqcqc.utils.annotation.CurrentLimit)")
    public void before(JoinPoint joinPoint) {
        log.debug("使用：{}，进行限流", limiterManager.getClass().getSimpleName());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String key;
        if (onMethod) {
            key = "global-limit-" + signature.getMethod().getName();
        } else {
            key = "global-limit";
        }
        Limiter limiter = Limiter.builder().limitNum(limitNum)
                .seconds(seconds)
                .key(key)
                .limitByUser(false)
                .build();

        boolean b;
        try {
            b = limiterManager.tryAccess(limiter);
        } catch (Exception e) {
            log.error("限流器：{}，发生异常：{}", limiterManager.getClass().getSimpleName(), e.getMessage());
            throw new ErrorTryAccessException(e.getMessage());
        }
        if (!b) {
            log.warn("接口：{}，已被限流  key：{}，在{}秒内访问次数超过{}，限流类型：{}",
                    method.getName(), key, limiter.getSeconds(),
                    limiter.getLimitNum(),
                    "全局限流");
            throw new ApiCurrentLimitException("there are too many people accessing the service, please try again later");
        }
    }
}
