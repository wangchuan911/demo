package org.welisdoon.webserver.vertx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Verticle
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2020/11/23 19:29
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Verticle {
    boolean worker() default false;
}
