package my.hehe.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import my.hehe.webserver.config.ClusterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("standaredVerticle")
public class StandaredVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(StandaredVerticle.class);

    @Value("${server.port}")
    private int SERVER_PORT;
    @Value("${server.https-enable}")
    private boolean IS_HTTPS;
    /**/
    private Set<Handler<Vertx>> VERTX_HANDLERS = null;
    private Set<Handler<Router>> ROUTER_HANDLERS = null;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        //开启https
        if (IS_HTTPS) {
            SelfSignedCertificate certificate = SelfSignedCertificate.create();
            httpServerOptions.setSsl(true)
                    .setKeyCertOptions(certificate.keyCertOptions())
                    .setTrustOptions(certificate.trustOptions());
        }

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        logger.info("create router");
        this.registeredHandler(Vertx.class, null);
        this.registeredHandler(Router.class, router1 -> {
            router1.get("/").handler(routingContext -> {
                routingContext.response().end("hello world!");
            });
        });

        for (Handler<Vertx> vertxHandler : VERTX_HANDLERS) {
            vertxHandler.handle(vertx);
        }
        for (Handler<Router> routerHandler : ROUTER_HANDLERS) {
            routerHandler.handle(router);
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

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }


    public void setClusterPeriodic(long time, String uniqeKey, Handler<Long> handler) {
        this.registeredHandler(Vertx.class, vertx1 -> {
            vertx1.setPeriodic(time, aLong -> {
                SharedData sharedData = vertx1.sharedData();
                sharedData.getLock("LOCK_" + uniqeKey, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        try {
                            handler.handle(aLong);
                        } finally {
                            Lock lock = lockAsyncResult.result();
                            vertx1.setTimer(5000, aLong1 -> {
                                lock.release();
                            });
                        }
                    } else {
                        logger.info(lockAsyncResult.cause().getMessage());
                    }
                });
            });
        });
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> void registeredHandler(Class<T> t, Handler<T> handler) {
        if (Vertx.class.getName().equals(t.getName())) {
            if (VERTX_HANDLERS == null) {
                VERTX_HANDLERS = new ConcurrentHashSet<>(4);
            }
            if (handler == null) return;
            VERTX_HANDLERS.add((Handler<Vertx>) handler);
        } else if (Router.class.getName().equals(t.getName())) {
            if (ROUTER_HANDLERS == null) {
                ROUTER_HANDLERS = new ConcurrentHashSet<>(4);
            }
            if (handler == null) return;
            ROUTER_HANDLERS.add((Handler<Router>) handler);
        } else {
            throw new RuntimeException("Error Object Type!");
        }
        {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            logger.info("registered Handler Router->" + stackTraceElement.getMethodName() + "[" + stackTraceElement.getLineNumber() + "]");
        }
    }

    /*public synchronized void registerdHandler(Handler<Router> handler) throws RuntimeException {
        if (registeredRouterHandlers == null) {
            registeredRouterHandlers = new ConcurrentHashSet<>(4);
        }
        registeredRouterHandlers.add(handler);
    }*/
}


