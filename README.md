# 💖SpringBoot💖  接口限流工具🥳

## 安装👋：

- maven🤩：

    ```xml
    <dependency>
      <groupId>online.zust.qcqcqc.utils</groupId>
      <artifactId>api-current-limiter-starter</artifactId>
      <version>1.0.6</version>
    </dependency>
    ```

    > 🥰已经发布在中央仓库：https://central.sonatype.com/artifact/online.zust.qcqcqc.utils/api-current-limiter-starter



## 使用🤏：

1. 🫰在配置文件中启用

    ```yaml
    limiter:
      enable: true
      type: redis
    ```

    这里的type支持多种类型，redis为默认实现之一，已经写好实现类😘

    - redis：使用redis进行限流控制。
    - 默认：使用ConcurrentHashMap进行限流控制。
    - 自定义：取消设置该选项可以使用自定义算法。

2. 🤞在接口上添加注解

    ```java
        @CurrentLimit(limitNum = 20, seconds = 10, limitByUser = true, key = "LimitTest", msg = "请求过于频繁")
        @GetMapping("/test")
        public Result<String> test() {
            return Result.success("test");
        }
    ```

    1. 注解中的seconds为规定时间内访问。
    2. 这里的limitNum为规定时间内的访问次数限制。
    3. *limitByUser为是否根据登录信息进行限制。
        1. false：全局限流，对该接口的所有请求进行记录
        2. true：根据getUserKey方法获取用户信息（默认为ip），进行限流
    4. key为标识，可以为空，为空时默认取方法名。
    5. 还可以指定msg，也就是错误信息。

    ```java
        @IntervalLimit(interval = 2000, limitByUser = true, key = "IntervalTest", msg = "请求过于频繁")
        @GetMapping("/test/3")
        public Result<String> test3() {
            return Result.success(ProxyUtil.getBean(LimiterManager.class).getClass().getSimpleName());
        }
    ```

    1. interval为间隔时间，单位毫秒
        - 这个注解用于实现对该接口的请求间隔限制（可用于防抖），默认值为100ms

    ```java
        @ConcurrentLimit(limitNum = 20, limitByUser = true, key = "ConcurrentTest", msg = "请求过于频繁")
        @GetMapping("/test/4")
        public Result<String> test4() {
            return Result.success(ProxyUtil.getBean(LimiterManager.class).getClass().getSimpleName());
        }
    ```

    1. limitNum：限制最大并发数
        - 这个注解用于实现对该接口的最大并发数限制，默认值为10

    **异常处理😟：**

    - 在禁止访问时会抛出异常：ApiCurrentLimitException，可以自定义异常处理器进行处理。
    - 在尝试访问失败时会抛出异常：ErrorTryAccessException，此后请求会被拒绝。

    ----

    ### 高级🤔

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

    - ##### ***这只是一个示例，假设limitByUser = true，并且想对业务用户进行限流，则必须实现LimiterConfig中的getUserKey方法，否则只会使用客户端IP进行限流***

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
    
        @Override
        public boolean checkInterval(boolean limitByUser, String key, long interval) {
            return false;
        }
    
        @Override
        public boolean checkConcurrent(boolean limitByUser, String key, int limitNum, boolean set) {
            return false;
        }
    }
    ```

    - 实现tryAccess方法，返回false时拒绝请求，true时允许请求。

        - 在使用CurrentLimit注解时会调用这个方法
        
        
        
    - 实现checkInterval方法，返回false时拒绝请求，true时允许请求。

        - 在使用IntervalLimit注解时会调用这个方法

        

    - 实现checkConcurrent方法，返回false时拒绝请求，true时允许请求。

      - 在使用ConcurrentLimit注解时会调用这个方法

          
        
        - 参考：
            online.zust.qcqcqc.utils.manager.BaseMapLimitManager（map + list实现）
        
            online.zust.qcqcqc.utils.manager.BaseRedisLimitManager（redis + lua实现）

5. 注解执行顺序

    ```
    ConcurrentLimitAspect >> IntervalLimitAspect >> CurrentLimitAspect
    ```

## 性能🙌

- 使用aop切面编程，在controller方法前切入，使用cglib代理生成动态代理类，对性能影响较小。
- 推荐使用redis（lua脚本保证原子性），性能更强

## 注意🙏

- 在自定义限流算法时，记得别忘了对并发请求的处理，避免实际限流值大于设定值。

## 展望☝️

- √ 请求间隔时间限制（防抖）√
- √ 对接口并发数进行限制 √
- 使用漏桶算法和令牌桶算法，实现QPS限流
- 请求队列 缓流（削峰填谷）
- 全局限流
- 拒绝策略
- 接口等待时间（？
- 支持SpringBoot3.0+

## 更新日志

- 1.0.0 正式发布
- 1.0.1 修改了POM文件
- 1.0.2 移除lombok依赖
- 1.0.3 将原有计数逻辑改为时间滑动窗口
- 1.0.4 添加接口请求间隔注解(可以用来实现防抖)
- 1.0.5 将Redis脚本移到资源目录，修复了过度占用Redis缓存的问题
- 1.0.6 新功能：接口最大并发数控制