package online.zust.qcqcqc.utils.config.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author qcqcqc
 * 全局限流功能开启条件
 * 通过配置文件中是否包含limiter.global.enable来判断是否开启全局限流功能
 */
public class GlobalLimitAspectCondition implements Condition {

    private static final Logger log = LoggerFactory.getLogger(GlobalLimitAspectCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取 limiter.global.enable 的值
        boolean b = Boolean.parseBoolean(context.getEnvironment().getProperty("limiter.global.enable"));
        log.info(b ? "全局限流功能已开启!" : "全局限流功能已关闭~");
        return b;
    }
}
