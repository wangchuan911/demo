package org.welisdoon.web.vertx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname VertxProxy
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 10:25
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxServiceProxy {
    Class<?> targetClass() default Object.class;

    String targetClassName() default "";

    String address();

}
