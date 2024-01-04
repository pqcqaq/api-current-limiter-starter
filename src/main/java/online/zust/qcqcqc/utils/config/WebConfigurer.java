package online.zust.qcqcqc.utils.config;

import online.zust.qcqcqc.utils.config.condition.GlobalLimitInterceptorCondition;
import online.zust.qcqcqc.utils.interceptor.GlobalLimitInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 和springmvc的webmvc拦截配置一样
 *
 * @author BIANP
 */
@Configuration
@Conditional(GlobalLimitInterceptorCondition.class)
public class WebConfigurer implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求
        log.info("全局限流功能已开启!");
        registry.addInterceptor(GlobalLimitInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public GlobalLimitInterceptor GlobalLimitInterceptor() {
        return new GlobalLimitInterceptor();
    }
}
