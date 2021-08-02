package org.welisdoon.webserver.vertx.verticle;

import io.vertx.core.Promise;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.welisdoon.webserver.vertx.annotation.Verticle;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Verticle(worker = true)
public class WorkerVerticle extends AbstractCustomVerticle {


    @Override
    void deployBefore(Promise future) {
        future.complete();
    }

    @Override
    void deployAfter(Promise future) {
        future.complete();
    }
}
