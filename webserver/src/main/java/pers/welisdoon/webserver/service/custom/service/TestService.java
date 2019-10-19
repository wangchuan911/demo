package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.welisdoon.webserver.common.web.CommonAsynService;
import pers.welisdoon.webserver.service.custom.dao.CustomDao;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Service
@VertxConfiguration
public class TestService {
    @Autowired
    CustomDao customDao;

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
                JsonArray jsonArray = new JsonArray()
                        .add("orderMapger")
                        .add(new JsonArray()
                                .add(1)
                                .add(new JsonObject().put("aa", "aa")));
                {
                    JsonArray arg3 = new JsonArray();
                    JsonObject jsonObject = new JsonObject();
                    {
                        Iterator<Map.Entry<String, String>> iterator = routingContext.request().params().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, String> stringEntry = iterator.next();
                            jsonObject.put(stringEntry.getKey(), stringEntry.getValue());
                        }
                        arg3.add(jsonObject);
                    }
                    {
                        Session session = routingContext.session();
                        Map<String, Object> sessionData = session != null ? session.data() : Map.of();
                        arg3.add(sessionData);
                    }
                    jsonArray.add(arg3);
                }
                commonAsynService.serviceCall("testService", jsonArray.getString(0), jsonArray.getJsonArray(1).toString(), jsonArray.getJsonArray(2).toString(), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });
            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            router.post("/wxApp").handler(routingContext -> {
                routingContext.response().setChunked(true);
                JsonArray jsonArray = routingContext.getBodyAsJsonArray();
                commonAsynService.serviceCall("testService", jsonArray.getString(0), jsonArray.getString(1), null, stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            logger.info("inital request mapping: /wxApp");
        };
        return routerConsumer;
    }


    public Object orderMapger(int mode, Map params) {
        return mode + params.toString();
    }

    public Object carManger(int mode, Map params) {
        return null;
    }

    public Object userManger(int mode, Map params) {
        return null;
    }
}
