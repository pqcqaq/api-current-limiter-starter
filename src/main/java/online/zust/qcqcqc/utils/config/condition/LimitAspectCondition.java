package online.zust.qcqcqc.utils.config.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pqcmm
 */
public class LimitAspectCondition implements Condition {

    private static final Logger log = LoggerFactory.getLogger(LimitAspectCondition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        //检查配置文件是否包含limit.enable
        boolean b = conditionContext.getEnvironment().containsProperty("limiter.enable");
        if (b) {
            log.info("限流功能已开启!");
        } else {
            log.info("限流功能已关闭");
        }
        return b;
    }
}
