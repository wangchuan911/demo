package org.welisdoon.webserver.vertx.annotation;

import io.netty.handler.codec.http.HttpMethod;
import org.welisdoon.webserver.vertx.enums.VertxRouteType;

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
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxRouter {
    String path();

    String[] method() default {};

    String produces() default "";

    String consumes() default "";

    VertxRouteType mode() default VertxRouteType.Path;

    int order() default 0;

}

