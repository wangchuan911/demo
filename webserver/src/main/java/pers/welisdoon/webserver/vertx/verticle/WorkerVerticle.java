package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkerVerticle extends AbstractCustomVerticle {


    @Override
    void registedBefore(Promise future) {
        future.complete();
    }

    @Override
    void registedAfter(Promise future) {
        future.complete();
    }
}
