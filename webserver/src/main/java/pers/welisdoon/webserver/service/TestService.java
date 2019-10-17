package pers.welisdoon.webserver.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.welisdoon.webserver.config.WeChatServiceConfiguration;
import pers.welisdoon.webserver.service.common.CommonAsynService;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import java.util.function.Consumer;

@Service
@VertxConfiguration
public class TestService {


    private static final Logger logger = LoggerFactory.getLogger(TestService.class);
    CommonAsynService commonAsynService;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            commonAsynService = CommonAsynService.createProxy(vertx1);
            System.out.println(commonAsynService);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
        Consumer<Router> routerConsumer = router -> {
            router.get("/wxApp").handler(routingContext -> {
                routingContext.response().setChunked(true);
                JsonArray jsonArray = new JsonArray().add("hehe").add(new JsonArray().add(1).add(2).add(3));
                commonAsynService.servceCall("testService", jsonArray.getString(0), jsonArray.getJsonArray(1).toString(), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });
            });
            router.post("/wxApp").handler(routingContext -> {
                routingContext.response().setChunked(true);
                JsonArray jsonArray = routingContext.getBodyAsJsonArray();
                commonAsynService.servceCall("testService", jsonArray.getString(0), jsonArray.getString(1), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

            });
            logger.info("inital request mapping: /wxApp");
        };
        return routerConsumer;
    }

    public String hehe(int a, int b, int c) {
        return a + b + c + "";
    }
}
