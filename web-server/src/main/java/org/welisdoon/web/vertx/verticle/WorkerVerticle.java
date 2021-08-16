package org.welisdoon.web.vertx.verticle;

import io.vertx.core.Promise;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Verticle(worker = true)
public class WorkerVerticle extends AbstractCustomVerticle {


    @Override
    void deploying(Promise future) {
        if (ApplicationContextProvider.getApplicationContext()
                .getBeansWithAnnotation(VertxServiceProxy.class).size() > 0) {
            AsyncProxyUtils.createServiceBinder(getVertx(), "common", IVertxInvoker.class);
        }
        future.complete();
    }

    @Override
    void deployComplete(Promise future) {
        future.complete();
    }
}
