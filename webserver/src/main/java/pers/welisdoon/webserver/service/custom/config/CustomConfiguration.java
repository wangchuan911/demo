package pers.welisdoon.webserver.service.custom.config;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import pers.welisdoon.webserver.common.web.CommonAsynService;
import pers.welisdoon.webserver.common.web.Requset;
import pers.welisdoon.webserver.service.custom.dao.TacheDao;
import pers.welisdoon.webserver.service.custom.dao.UserDao;
import pers.welisdoon.webserver.service.custom.entity.TacheVO;
import pers.welisdoon.webserver.service.custom.service.RequestService;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

@Configuration
@ConfigurationProperties("wechat-app")
@VertxConfiguration
public class CustomConfiguration extends AbstractWechatConfiguration {
    final static String REQUEST_NAME = "requestService";
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    CommonAsynService commonAsynService;
    @Autowired
    RequestService requestService;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            commonAsynService = CommonAsynService.createProxy(vertx1);
            System.out.println(commonAsynService);
        };
        return vertxConsumer;
    }

    @PostConstruct
    void initValue() {

        TacheDao tacheDao = ApplicationContextProvider.getBean(TacheDao.class);
        UserDao userDao = ApplicationContextProvider.getBean(UserDao.class);
        CustomConst.TACHE.initTacheMapValue(tacheDao.listAll(new TacheVO().setTampalateId(1)));
        CustomConst.ROLE.initRoleMapValue(userDao.listRoles());


    }

    /*定時任務*/
    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createOrdderTimer() {
        final String KEY = this.getClass().getName().toUpperCase();
        final String URL_TOCKEN_LOCK = KEY + ".LOCK";
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            SharedData sharedData = vertx1.sharedData();
            Handler<Long> longHandler = aLong -> {
                /*集群锁，防止重复处理和锁表*/
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        try {
                            requestService.toBeContinue();
                        } finally {
                            Lock lock = lockAsyncResult.result();
                            vertx1.setTimer(3 * 1000, aLong1 -> {
                                lock.release();
                            });
                        }
                    } else {
                        logger.info(lockAsyncResult.cause().getMessage());
                    }
                });
            };
            /*5s*/
            vertx1.setPeriodic(5 * 1000, longHandler);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
        final String PATH_WX_APP = "/wxApp";
        final String PATH_WX_APP_UPLOAD = "/imgUpd";
        final String URL_CODE_2_SESSION = this.getUrls().get("code2Session").toString();
        WebClient webClient = WebClient.create(vertx);
        Consumer<Router> routerConsumer = router -> {
            router.get(PATH_WX_APP).handler(routingContext -> {
                routingContext.response().setChunked(true);
                MultiMap multiMap = routingContext.request().params();
                int code;
                switch (code = Integer.parseInt(multiMap.get("code"))) {
                    case -1:
                        webClient.getAbs(URL_CODE_2_SESSION + multiMap.get("value"))
                                .send(httpResponseAsyncResult -> {
                                    if (httpResponseAsyncResult.succeeded()) {
                                        HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                        JsonObject jsonObject = httpResponse.body().toJsonObject();
                                        String key = jsonObject.remove("session_key").toString();
                                        String userId = jsonObject.getString("openid");
                                        /*if (!StringUtils.isEmpty(userId)) {
                                            Object o = testService.userManger(TestService.GET, Map.of("id", userId));
                                            UserVO userVO;
                                            if (o == null) {
                                                userVO = new UserVO().setId(userId);
                                                userVO.setRole(0);
                                                userVO.setName("新用戶");
                                                jsonObject.put("user", JsonObject.mapFrom(userVO));
                                            }
                                            else {
                                                userVO = (UserVO) o;
                                                jsonObject.put("user", o);
                                                switch (userVO.getRole()) {
                                                    case 0:
                                                        o = testService.tacheManager(TestService.GET_WORK_NUMBER, Map.of("userId", userVO.getId()));
                                                        break;
                                                    default:
                                                        o = testService.orderManger(TestService.GET_WORK_NUMBER, Map.of("custId", userVO.getId()));
                                                }
                                                o=o!=null?JsonObject.mapFrom(o):Map.of("all_nums",0,"nums",0);
                                                jsonObject.put("work",o);
                                            }

                                        }*/
                                        jsonObject.mergeIn((JsonObject) requestService.login(userId));
                                        routingContext.response().end(jsonObject.toBuffer());
                                    } else {
                                        routingContext.fail(httpResponseAsyncResult.cause());
                                    }
                                });
                        break;
                    default:
                        JsonArray jsonArray = new JsonArray()
                                .add("otherManager")
                                .add(new JsonArray()
                                        .add(code)
                                        .add(new JsonArray("value")));
                        jsonArray.add(this.getPrams(routingContext));
                        commonAsynService.serviceCall(REQUEST_NAME, jsonArray.getString(0), jsonArray.getJsonArray(1).toString(), jsonArray.getJsonArray(2).toString(), stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.response().end(stringAsyncResult.result());
                                System.out.println();
                            } else {
                                routingContext.fail(500, stringAsyncResult.cause());
                            }
                        });
                        break;
                }

            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            router.post(PATH_WX_APP).handler(routingContext -> {
                routingContext.response().setChunked(true);
                JsonArray jsonArray = routingContext.getBodyAsJsonArray();
                Requset requset = new Requset()
                        .setService(REQUEST_NAME)
                        .setMethod(jsonArray.getString(0))
                        .setBody(jsonArray.getJsonArray(1))
                        .putParams(routingContext.request().params())
                        .putSession(routingContext.session());
                commonAsynService.requsetCall(requset, stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result().toJson().toBuffer());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });

            logger.info("inital request mapping: " + PATH_WX_APP);

            router.post(PATH_WX_APP_UPLOAD).blockingHandler(routingContext -> {
                try {
                    HttpServerRequest httpServerRequest = routingContext.request();
                    HttpServerResponse httpServerResponse = routingContext.response();
                    Set<FileUpload> fileUploads = routingContext.fileUploads();
                    if (fileUploads == null || fileUploads.size() != 1) {
                        httpServerResponse.setStatusCode(404).end();
                        return;
                    }
                    FileUpload fileUpload = fileUploads.iterator().next();
                    Requset requset = new Requset()
                            .setService(REQUEST_NAME)
                            .setMethod("uploadFile")
                            .setBody(new JsonArray()
                                    .add(new JsonObject()
                                            .put("uploadedFileName", fileUpload.uploadedFileName())
                                            .put("name", fileUpload.name())
                                            .put("charSet", fileUpload.charSet())
                                            .put("contentType", fileUpload.contentType())
                                            .put("size", fileUpload.size())
                                            .put("fileName", fileUpload.fileName())
                                            .put("contentTransferEncoding", fileUpload.contentTransferEncoding()))
                                    .add(JsonObject.mapFrom(httpServerRequest.formAttributes().entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))))
                            .putParams(httpServerRequest.params())
                            .putSession(routingContext.session());
                    commonAsynService.requsetCall(requset, stringAsyncResult -> {
                        if (stringAsyncResult.succeeded()) {
                            httpServerResponse.end(stringAsyncResult.result().toJson().toBuffer());
                        } else {
                            routingContext.fail(500, stringAsyncResult.cause());
                        }
                        vertx.fileSystem().delete(fileUpload.uploadedFileName(), voidAsyncResult -> {
                        });
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    routingContext.fail(500, e);
                }

            });

            logger.info("inital request mapping: " + PATH_WX_APP_UPLOAD);

            router.get("/imgDl/:picType/:picId").handler(routingContext -> {
                String picType = routingContext.request().getParam("picType");
                String picId = routingContext.request().getParam("picId");
                routingContext.response().sendFile("");
            });

        };
        return routerConsumer;
    }

    private JsonArray getPrams(RoutingContext routingContext) {
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
            return arg3;
        }
    }
}
