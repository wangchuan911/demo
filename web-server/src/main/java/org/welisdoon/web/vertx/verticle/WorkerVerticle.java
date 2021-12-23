package org.welisdoon.web.vertx.verticle;

import io.vertx.core.Promise;

import io.vertx.core.Vertx;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.Pool;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Verticle(worker = true)
public class WorkerVerticle extends AbstractCustomVerticle {

    static Pool<Vertx> pool = new Pool<>();

    @Override
    void deploying(Promise future) {
        future.complete();
    }

    @Override
    void deployComplete(Promise future) {
        future.complete();
        pool.add(vertx);
    }

    public static Vertx getOneVertx() {
        return pool.getCacheOne();
    }

    public static Vertx getOneVertx(boolean isNew) {
        return pool.getOne();
    }

    public static Vertx[] getAllVertxs() {
        return pool.getAll();
    }
}
