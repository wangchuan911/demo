package org.welisdoon.web.vertx.annotation;

import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.verticle.AbstractWebVerticle;
import org.welisdoon.web.vertx.verticle.MainWebVerticle;

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
    String prefix();

    Class<? extends AbstractWebVerticle> verticle() default MainWebVerticle.class;

    boolean requestBodyEnable() default false;
}

