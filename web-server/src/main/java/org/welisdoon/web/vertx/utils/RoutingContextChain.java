package org.welisdoon.web.vertx.utils;

import com.github.pagehelper.PageInfo;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.web.entity.User;

import java.util.List;
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

    /*public static <T> Future<Void> simple(RoutingContext context, Class<T> type, Function<T, Object> function) {
        return context.end(JsonUtils.asJsonString(function.apply(JsonUtils.toBean(context.body().asString(), type))));
    }

    public static <T extends BaseCondition> Future<Void> page(RoutingContext context, Class<T> type, Function<T, List> function) {
        T condition = JsonUtils.toBean(context.body().asString(), type);
        condition.startPage();
        return context.end(JsonUtils.asJsonString(PageInfo.of(function.apply(condition))));
    }*/
}
