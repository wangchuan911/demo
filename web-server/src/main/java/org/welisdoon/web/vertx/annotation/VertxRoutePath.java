package org.welisdoon.web.vertx.annotation;

import org.welisdoon.web.vertx.enums.VertxRouteType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname VertxRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/1/27 01:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxRoutePath {
    String value();
}

