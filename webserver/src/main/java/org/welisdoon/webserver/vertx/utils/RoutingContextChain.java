package org.welisdoon.webserver.vertx.utils;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.welisdoon.webserver.common.Chain;

import java.lang.ref.SoftReference;

/**
 * @Classname RoutingContextChain
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/1/27 18:49
 */
public class RoutingContextChain extends Chain<Handler<RoutingContext>> {

    Handler<RoutingContext> failureHandler;

    @Override
    public RoutingContextChain add(Handler<RoutingContext> fun) {
        return (RoutingContextChain) super.add(fun);
    }

    public RoutingContextChain fail(Handler<RoutingContext> fun) {
        this.failureHandler = fun;
        return this;
    }

    public Handler<RoutingContext> getFailureHandler() {
        return failureHandler;
    }

    @Override
    public void release() {
        super.release();
        failureHandler = null;
        SoftReference<RoutingContextChain> reference=new SoftReference<>(this);
    }
}
