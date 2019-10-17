package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class AbstractWebVerticle extends AbstractCustomVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebVerticle.class);

    int port;
    boolean sslEnable;
    String sslKeyStore;
    String sslPassword;

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
            if (sslEnable) {
                httpServerOptions.setSsl(true);
                if (!StringUtils.isEmpty(sslKeyStore)) {
                    httpServerOptions.setKeyCertOptions(new JksOptions().setPath(sslKeyStore).setPassword(sslPassword));
                } else {
                    SelfSignedCertificate certificate = SelfSignedCertificate.create();
                    httpServerOptions.setKeyCertOptions(certificate.keyCertOptions())
                            .setTrustOptions(certificate.trustOptions());
                }
            }
            vertx.createHttpServer(httpServerOptions)
                    .requestHandler(router)
                    .listen(port, httpServerAsyncResult -> {
                        if (httpServerAsyncResult.succeeded()) {
                            startFuture.complete();
                            logger.info("HTTP server started on http" + (sslEnable ? "s" : "") + "://localhost:" + port);
                        } else {
                            startFuture.fail(httpServerAsyncResult.cause());
                        }
                    });
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public void setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }
}


