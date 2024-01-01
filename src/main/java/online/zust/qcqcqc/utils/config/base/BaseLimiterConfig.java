package online.zust.qcqcqc.utils.config.base;

import online.zust.qcqcqc.utils.config.LimiterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qcqcqc
 */
@Configuration
@ConditionalOnMissingBean(LimiterConfig.class)
public class BaseLimiterConfig implements LimiterConfig {

    private HttpServletRequest httpServletRequest;

    @Autowired
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public String getUserKey() {
        return httpServletRequest.getRemoteAddr();
    }
}
