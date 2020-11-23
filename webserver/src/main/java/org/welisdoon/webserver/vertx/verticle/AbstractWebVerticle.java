package org.welisdoon.webserver.vertx.verticle;

import java.io.File;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebVerticle extends AbstractCustomVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebVerticle.class);

    int port;
    boolean sslEnable;
    String sslKeyStore;
    String sslPassword;
    String sslKeyType;
    String sslKeyPath;

    private Router router;

    @Override
    void deployBefore(Promise startFuture) {
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        logger.info("create router");
        startFuture.complete();

    }

    @Override
    void deployAfter(Promise startFuture) {
        if (router != null) {
            //开启https
            HttpServerOptions httpServerOptions = new HttpServerOptions();
            if (!new File(sslKeyStore).exists()) {
                logger.warn(String.format("sslKeyStore:%s is not exists!", sslKeyStore));
            } else if (sslEnable) {
                httpServerOptions.setSsl(true);
                switch (this.sslKeyType.toLowerCase()) {
                    case "pem":
                        httpServerOptions.setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(sslKeyStore).setKeyPath(sslKeyPath));
                        break;
                    case "jks":
                        httpServerOptions.setKeyCertOptions(new JksOptions().setPath(sslKeyStore).setPassword(sslPassword));
                        break;
                    case "pfx":
                        httpServerOptions.setPfxKeyCertOptions(new PfxOptions().setPath(sslKeyStore).setPassword(sslPassword));
                        break;
                    default:
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

    public String getSslKeyType() {
        return sslKeyType;
    }

    public void setSslKeyType(String sslKeyType) {
        this.sslKeyType = sslKeyType;
    }

    public String getSslKeyPath() {
        return sslKeyPath;
    }

    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }
}


