package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import pers.welisdoon.webserver.config.ClusterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("standaredVerticle")
public class StandaredVerticle extends AbstractCustomVerticle {

    private static final Logger logger = LoggerFactory.getLogger(StandaredVerticle.class);

    @Value("${server.port}")
    private int SERVER_PORT;
    @Value("${server.https-enable}")
    private boolean IS_HTTPS;

    Router router;

    @Override
    void registedBefore(Future startFuture) {
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        logger.info("create router");
        startFuture.complete();
        /*this.registeredHandler(Vertx.class, null);
        this.registeredHandler(Router.class, router1 -> {
            router1.get("/").handler(routingContext -> {
                routingContext.response().end("hello world!");
            });
        });

        this.startRegister();
        if (router != null) {
            vertx.createHttpServer(httpServerOptions)
                    .requestHandler(router)
                    .listen(SERVER_PORT, httpServerAsyncResult -> {
                        if (httpServerAsyncResult.succeeded()) {
                            startFuture.complete();
                            logger.info("HTTP server started on http" + (IS_HTTPS ? "s" : "") + "://localhost:" + SERVER_PORT);
                        } else {
                            startFuture.fail(httpServerAsyncResult.cause());
                        }
                    });
        }*/
    }

    @Override
    void registedAfter(Future startFuture) {
        if (router != null) {
            //开启https
            HttpServerOptions httpServerOptions = new HttpServerOptions();
            if (IS_HTTPS) {
                SelfSignedCertificate certificate = SelfSignedCertificate.create();
                httpServerOptions.setSsl(true)
                        .setKeyCertOptions(certificate.keyCertOptions())
                        .setTrustOptions(certificate.trustOptions());
            }
            vertx.createHttpServer(httpServerOptions)
                    .requestHandler(router)
                    .listen(SERVER_PORT, httpServerAsyncResult -> {
                        if (httpServerAsyncResult.succeeded()) {
                            startFuture.complete();
                            logger.info("HTTP server started on http" + (IS_HTTPS ? "s" : "") + "://localhost:" + SERVER_PORT);
                        } else {
                            startFuture.fail(httpServerAsyncResult.cause());
                        }
                    });
        }
    }

}


