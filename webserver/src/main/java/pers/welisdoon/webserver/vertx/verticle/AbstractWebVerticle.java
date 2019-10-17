package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pers.welisdoon.webserver.vertx.throwable.CreateVerticleInstanceError;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractWebVerticle extends AbstractCustomVerticle {

    final private static AtomicBoolean IS_SINGLE = new AtomicBoolean(true);

    public AbstractWebVerticle() {
        if (!IS_SINGLE.compareAndSet(true, false)) {
            throw new CreateVerticleInstanceError(0);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebVerticle.class);

    @Value("${server.port}")
    private int SERVER_PORT;
    @Value("${server.https.enable}")
    private boolean IS_HTTPS;
    @Value("${server.https.keyStore}")
    private String PATH_KEY_STORE;
    @Value("${server.https.password}")
    private String KEY_STORE_PASSWORD;

    private Router router;

    @Override
    void registedBefore(Future startFuture) {
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        logger.info("create router");
        startFuture.complete();

    }

    @Override
    void registedAfter(Future startFuture) {
        if (router != null) {
            //开启https
            HttpServerOptions httpServerOptions = new HttpServerOptions();
            if (IS_HTTPS) {
                httpServerOptions.setSsl(true);
                if (!StringUtils.isEmpty(PATH_KEY_STORE)) {
                    httpServerOptions.setKeyCertOptions(new JksOptions().setPath(PATH_KEY_STORE).setPassword(KEY_STORE_PASSWORD));
                } else {
                    SelfSignedCertificate certificate = SelfSignedCertificate.create();
                    httpServerOptions.setKeyCertOptions(certificate.keyCertOptions())
                            .setTrustOptions(certificate.trustOptions());
                }
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

    @Override
    public void stop() throws Exception {
        IS_SINGLE.set(true);
    }


}


