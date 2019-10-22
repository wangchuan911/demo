package pers.welisdoon.webserver.service.custom.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import pers.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import pers.welisdoon.webserver.common.web.CommonAsynService;
import pers.welisdoon.webserver.service.custom.service.TestService;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

@Configuration
@ConfigurationProperties("wechat-app")
@VertxConfiguration
public class TestConfiguration extends AbstractWechatConfiguration {

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
                        .add("orderManger")
                        .add(new JsonArray()
                                .add(1)
                                .add(new JsonObject().put("orderId", "1")));
                this.getPrams(jsonArray, routingContext);
                commonAsynService.serviceCall("testService", jsonArray.getString(0), jsonArray.getJsonArray(1).toString(), jsonArray.getJsonArray(2).toString(), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                        System.out.println();
                    }
                    else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });
            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            router.post("/wxApp").handler(routingContext -> {
                routingContext.response().setChunked(true);
                JsonArray jsonArray = routingContext.getBodyAsJsonArray();
                this.getPrams(jsonArray, routingContext);
                commonAsynService.serviceCall("testService", jsonArray.getString(0), jsonArray.getJsonArray(1).toString(), jsonArray.getJsonArray(2).toString(), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result());
                    }
                    else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            {
                Router subRouter = Router.router(vertx);
                subRouter.post("/imgUpd").blockingHandler(routingContext -> {
                    HttpServerRequest httpServerRequest = routingContext.request();
                    Set<FileUpload> fileUploads = routingContext.fileUploads();
                    Iterator<FileUpload> iterator = fileUploads.iterator();
                    FileUpload fileUpload = null;
                    while (iterator.hasNext()) {
                        fileUpload = iterator.next();
                        break;
                    }
                    File file = null;
                    if (fileUpload != null && (file = new File(fileUpload.uploadedFileName())).exists()) {
                        try {
                            byte[] bytes = new FileInputStream(file).readAllBytes();
                            String id = httpServerRequest.params().get("id");
                            String relaId = httpServerRequest.params().get("relaId");
                            String typeId = httpServerRequest.params().get("typeId");
                            /*customDao.saveImage(Map.of("picture_id", id,
                                    "picture_name", fileUpload.fileName(),
                                    "picture_storage", bytes,
                                    "related_id", relaId,
                                    "related_type_id", typeId));*/

                        }
                        catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                });
                subRouter.post("/imgDl/:picType/:picId").handler(routingContext -> {
                    String picType = routingContext.request().getParam("picType");
                    String picId = routingContext.request().getParam("picId");
                    routingContext.response().sendFile("");
                });
                router.mountSubRouter("/wxApp", subRouter);
            }
            logger.info("inital request mapping: /wxApp");
        };
        return routerConsumer;
    }

    private void getPrams(JsonArray jsonArray, RoutingContext routingContext) {
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
    }
}