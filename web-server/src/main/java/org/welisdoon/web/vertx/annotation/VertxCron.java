package org.welisdoon.web.vertx.annotation;

/**
 * @Classname VertxCron
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/25 22:25
 */
public @interface VertxCron {
    String expression();

    boolean immediate();
}
