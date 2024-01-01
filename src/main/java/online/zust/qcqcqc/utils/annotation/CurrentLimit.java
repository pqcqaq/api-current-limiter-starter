package online.zust.qcqcqc.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pqcmm
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentLimit {
    int limitNum() default 10;
    int seconds() default 60;
    String key() default "";
    boolean limitByUser() default false;
    String msg() default "There are currently many people , please try again later!";
}
