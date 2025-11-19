package org.welisdoon.web.vertx.utils;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RoutingContextChain.class);
    Route route;

    public RoutingContextChain() {

    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public <T> RoutingContextChain respond(Function<RoutingContext, Future<T>> function) {
        route.respond(routingContext -> {
            try {
                return function.apply(routingContext);
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
                return Future.failedFuture(e);
            }
        });
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
        route.handler(event -> handler(event, handler));
        return this;
    }

    protected void handler(RoutingContext routingContext, Handler<RoutingContext> handler) {
        try {
            handler.handle(routingContext);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            routingContext.fail(e);
        }
    }


    public RoutingContextChain blockingHandler(Handler<RoutingContext> handler) {
        route.blockingHandler(event -> handler(event, handler));
        return this;
    }

    public RoutingContextChain blockingHandler(Handler<RoutingContext> handler, boolean b) {
        route.blockingHandler(event -> handler(event, handler), b);
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
