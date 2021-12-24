package org.welisdoon.web.vertx.verticle;

import io.vertx.core.Promise;

import io.vertx.core.Vertx;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.Pool;
import org.welisdoon.web.vertx.annotation.Verticle;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Verticle(worker = true)
public class WorkerVerticle extends AbstractCustomVerticle {

    static Pool<Vertx> pool = new Pool<>();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        pool.add(vertx);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        pool.del(vertx);
    }

    public static Pool<Vertx> pool() {
        return pool;
    }


}
