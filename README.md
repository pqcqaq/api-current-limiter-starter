# SpringBoot  接口限流工具

## 安装👋：

- maven：还没传到中央仓库，先手动添加jar包吧



## 使用🤏：

1. 🫰在配置文件中启用

    ```yaml
    limiter:
      enable: true
      type: redis
    ```

    这里的type支持多种类型，redis为默认实现之一，已经写好实现类

    - redis：使用redis进行限流控制。
    - 默认：使用ConcurrentHashMap进行限流控制。
    - 自定义：取消设置该选项可以使用自定义算法。

2. 🤞在接口上添加注解

    ```java
        @CurrentLimit(limitNum = 20, seconds = 10, limitByUser = true, key = "LimitTest")
        @GetMapping("/test")
        public Result<String> test() {
            return Result.success("test");
        }
    ```

    1. 注解中的seconds为规定时间内访问。
    2. 这里的limitNum为规定时间内的访问次数限制。
    3. *limitByUser为是否根据登录信息进行限制（默认为全局或ip地址）。
        1. false：全局限流，对该接口的所有请求进行记录
        2. true：根据getUserKey方法获取用户信息（默认为ip），进行限流
    4. key为标识，可以为空，为空时默认取方法名。

    ----

    ### 高级

3. **（可选）**🤘自定义获取key生成方法

    ```java
    /**
     * @author pqcmm
     */
    @Component
    public class MyLimiterConfig implements LimiterConfig {
        @Setter(onMethod_ = {@Autowired})
        private HttpServletRequest httpServletRequest;
    
        @Override
        public String getUserKey() {
            // 如果已经登录，则使用用户名作为唯一标识
            User currentUser = ContextUtil.getCurrentUser();
            if (currentUser == null) {
                // 否则使用IP地址作为唯一标识
                return httpServletRequest.getRemoteAddr();
            }
            return currentUser.getUsername();
        }
    }
    ```

    - ##### ***这只是一个示例，假设limitByUser = true，则必须实现LimiterConfig中的getUserKey方法，否则只会使用客户端IP进行限流***

4. **（可选）**🫵自定义限流算法

    1. 取消设置 limiter.type
    2. 编写LimitManager类

    ```java
    /**
     * @author qcqcqc
     */
    @Component
    public class TestLimiterManager implements LimiterManager {
        @Override
        public boolean tryAccess(Limiter limiter) {
            return false;
        }
    }
    ```

    - 实现tryAccess方法，返回false时拒绝请求，true时允许请求。

        - 参考：
            online.zust.qcqcqc.utils.manager.BaseMapLimitManager（map实现）

            online.zust.qcqcqc.utils.manager.BaseRedisLimitManager（redis实现）

## 性能🙌

- 使用aop切面编程，在controller方法前切入，使用cglib代理生成动态代理类，对性能影响约为2ms

## 注意🙏

- 在自定义限流算法时，记得别忘了对并发请求的处理，避免实际限流值大于设定值。