package org.welisdoon.web.vertx.utils;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.welisdoon.web.entity.User;

import java.util.function.Function;

/**
 * @Classname RoutingContextChain
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/1/27 18:49
 */
public class RoutingContextChain {
    static ThreadLocal<User> threadLocal = new InheritableThreadLocal<>();
    Route route;

    public RoutingContextChain() {

    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public <T> RoutingContextChain respond(Function<RoutingContext, Future<T>> function) {
        route.respond(function);
        return this;
    }


    /*public RoutingContextChain order(int i) {
        route.order(i);
        return this;
    }*/


    public RoutingContextChain last() {
        route.last();
        return this;
    }


    public RoutingContextChain handler(Handler<RoutingContext> handler) {
        route.handler(handler);
        return this;
    }


    public RoutingContextChain blockingHandler(Handler<RoutingContext> handler) {
        route.blockingHandler(handler);
        return this;
    }

    public RoutingContextChain blockingHandler(Handler<RoutingContext> handler, boolean b) {
        route.blockingHandler(handler, b);
        return this;
    }


    public RoutingContextChain failureHandler(Handler<RoutingContext> handler) {
        route.failureHandler(handler);
        return this;
    }
}
