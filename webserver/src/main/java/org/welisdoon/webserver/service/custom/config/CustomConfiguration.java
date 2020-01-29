package org.welisdoon.webserver.service.custom.config;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.JAXBUtils;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PrePayResponseMesseage;
import org.welisdoon.webserver.service.custom.entity.OrderVO;
import org.welisdoon.webserver.service.custom.service.*;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.service.custom.dao.TacheDao;
import org.welisdoon.webserver.service.custom.entity.TacheVO;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;

@Configuration
@ConfigurationProperties("wechat-app")
@VertxConfiguration
public class CustomConfiguration extends AbstractWechatConfiguration {
//    final static String REQUEST_NAME = "requestService";
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private int orderCycleTime;

    public Integer getOrderCycleTime() {
        return orderCycleTime;
    }

    public void setOrderCycleTime(int orderCycleTime) {
        this.orderCycleTime = orderCycleTime;
    }

    ICommonAsynService commonAsynService;

    //    RequestService requestService;
//    TacheSerivce tacheSerivce;
    OrderService orderService;
    UserService userService;
    OperationService operationService;
//    CarSerivce carSerivce;
//    PictureSerivce pictureSerivce;
//    EvaluateSerivce evaluateSerivce;

    @Value("${temp.filePath}")
    String staticPath;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            commonAsynService = ICommonAsynService.createProxy(vertx1);
            System.out.println(commonAsynService);
        };
        return vertxConsumer;
    }

    @PostConstruct
    void initValue() {
        TacheDao tacheDao = ApplicationContextProvider.getBean(TacheDao.class);
//        requestService = ApplicationContextProvider.getBean(RequestService.class);
//        tacheSerivce = ApplicationContextProvider.getBean(TacheSerivce.class);
        orderService = ApplicationContextProvider.getBean(OrderService.class);
        userService = ApplicationContextProvider.getBean(UserService.class);
        operationService = ApplicationContextProvider.getBean(OperationService.class);
//        carSerivce = ApplicationContextProvider.getBean(CarSerivce.class);
//        pictureSerivce = ApplicationContextProvider.getBean(PictureSerivce.class);
//        evaluateSerivce = ApplicationContextProvider.getBean(EvaluateSerivce.class);
        CustomConst.TACHE.initTacheMapValue(tacheDao.listAll(new TacheVO().setTampalateId(1)));
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
//                            requestService.toBeContinue();
                            operationService.toBeContinue();
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
            if (this.getOrderCycleTime() > 0)
                vertx1.setPeriodic(this.getOrderCycleTime() * 1000, longHandler);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
//        final String PATH_WX_APP = "/wxApp(?:/([^\\/]+))*";
        final String PATH_WX_APP = "/wxApp";
        final String PATH_WX_APP_UPLOAD = "/imgUpd";
        final String URL_CODE_2_SESSION = this.getUrls().get("code2Session").toString();
        final String URL_UNIFIEDORDERON = this.getUrls().get("unifiedorder").toString();
        final String PATH_PRROJECT = this.getClass().getResource("/").getPath();
        WebClient webClient = WebClient.create(vertx);
        Consumer<Router> routerConsumer = router -> {
            router.get(PATH_WX_APP).handler(routingContext -> {
                routingContext.response().setChunked(true);
                MultiMap multiMap = routingContext.request().params();
                switch (Integer.parseInt(multiMap.get("code"))) {
                    case CustomConst.OTHER.LOGIN:
                        webClient.getAbs(URL_CODE_2_SESSION + multiMap.get("value"))
                                .send(httpResponseAsyncResult -> {
                                    if (httpResponseAsyncResult.succeeded()) {
                                        HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                        JsonObject jsonObject = httpResponse.body().toJsonObject();
                                        String key = jsonObject.remove("session_key").toString();
                                        String userId = jsonObject.getString("openid");
//                                        jsonObject.mergeIn((JsonObject) requestService.login(userId));
                                        jsonObject.mergeIn((JsonObject) userService.login(userId));
                                        routingContext.response().end(jsonObject.toBuffer());
                                    } else {
                                        routingContext.fail(httpResponseAsyncResult.cause());
                                    }
                                });
                        break;
                    case CustomConst.OTHER.PRE_PAY:
                        try {
                            OrderVO orderVo = (OrderVO) orderService.handle(CustomConst.GET, Map.of("orderId", multiMap.get("orderId")));
                            Buffer buffer = Buffer.buffer(JAXBUtils.toXML(new PrePayRequsetMesseage()
                                    .setAppId(this.getAppID())
                                    .setMchId(this.getMchId())
                                    .setNonceStr(multiMap.get("nonce"))
                                    .setBody(this.getAppName() + "-服务费用结算")
                                    .setOutTradeNo(orderVo.getOrderCode())
                                    .setTotalFee((int) 10.0)
                                    .setSpbillCreateIp(InetAddress.getLocalHost().getHostAddress())
                                    .setNotifyUrl(multiMap.get("notifyUrl"))
                                    .setTradeType("JSAPI")
                                    .setOpenid(orderVo.getCustId())
                                    .setSign(null)
                            ));
                            webClient.postAbs(URL_UNIFIEDORDERON)
                                    .sendBuffer(buffer, httpResponseAsyncResult -> {
                                        if (httpResponseAsyncResult.succeeded()) {
                                            try {
                                                PrePayResponseMesseage prePayResponseMesseage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), PrePayResponseMesseage.class);
                                                System.out.println(prePayResponseMesseage.getReturnMsg());
                                                System.out.println(prePayResponseMesseage.getErrCodeDes());
                                                System.out.println(prePayResponseMesseage.getErrCode());
                                                String sign = String.format("appId=%s&nonceStr=%s&package=prepay_id=%s&signType=MD5&timeStamp=%s"
                                                        , this.getAppID()
                                                        , multiMap.get("nonceStr")
                                                        , prePayResponseMesseage.getPrepayId()
                                                        , multiMap.get("timeStamp"));
                                                routingContext.response().end(new JsonObject().put("sign", sign).toBuffer());
                                            } catch (Throwable t) {
                                                routingContext.fail(httpResponseAsyncResult.cause());
                                            }
                                        } else {
                                            routingContext.fail(httpResponseAsyncResult.cause());
                                        }
                                    });
                        } catch (Throwable t) {
                            routingContext.fail(t);
                        }
                        break;
                    default:

                        break;
                }

            }).failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
            router.post(PATH_WX_APP).handler(routingContext -> {
                routingContext.response().setChunked(true);
//                JsonArray jsonArray = routingContext.getBodyAsJsonArray();
                /*Requset requset = new Requset()
                        .setService(REQUEST_NAME)
                        .setMethod(jsonArray.getString(0))
                        .setBody(jsonArray.getJsonArray(1).toString())
                        .putParams(routingContext.request().params())
                        .putSession(routingContext.session());*/
                Requset requset = Requset.newInstance(Requset.SIMPLE_REQUEST, routingContext);//.setService(REQUEST_NAME);
                commonAsynService.requsetCall(requset, stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result().toJson().toBuffer());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

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
                    /*Requset requset = new Requset()
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
                                    .add(JsonObject.mapFrom(httpServerRequest.formAttributes().entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))).toString())
                            .putParams(httpServerRequest.params())
                            .putSession(routingContext.session());*/
                    Requset requset = Requset.newInstance(Requset.UPLOAD_FILE, routingContext);//.setService(REQUEST_NAME);
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
            StaticHandler staticHandler = StaticHandler.create()
                    .setAllowRootFileSystemAccess(true)
                    .setWebRoot(staticPath);
            staticHandler.setAlwaysAsyncFS(true);
            staticHandler.setCachingEnabled(false);
//            staticHandler.setDirectoryListing(true);
//            staticHandler.setFilesReadOnly(false);
            router.get("/pic/*").handler(routingContext -> {
                HttpServerRequest httpServerRequest = routingContext.request();
                String fileName = Utils.pathOffset(httpServerRequest.path(), routingContext);
                String file = staticPath + fileName;
                FileSystem fileSystem = routingContext.vertx().fileSystem();
                fileSystem.exists(file, booleanAsyncResult -> {
                    if (booleanAsyncResult.succeeded() && !booleanAsyncResult.result()) {
                        Requset requset = new Requset()
                                .setService("pictureSerivce")
                                .setMethod("handle")
                                .setBody(new JsonArray()
                                        .add(CustomConst.GET)
                                        .add(Map.of("name", fileName.substring(1))).toString())
                                .putParams(httpServerRequest.params())
                                .putSession(routingContext.session());
                        commonAsynService.requsetCall(requset, responseAsyncResult -> {
                            if (responseAsyncResult.failed()) {
                                routingContext.fail(500, responseAsyncResult.cause());
                                return;
                            }
                            if (responseAsyncResult.result() == null || responseAsyncResult.result().getResult() == null) {
                                routingContext.fail(404);
                                return;
                            }
                            fileSystem.createFile(file, voidAsyncResult -> {
                                if (voidAsyncResult.failed()) {
                                    routingContext.fail(500, voidAsyncResult.cause());
                                    return;
                                }
                                fileSystem.writeFile(file, Buffer.buffer(((JsonObject) responseAsyncResult.result().getResult()).getBinary("data")), voidAsyncResult1 -> {
                                    if (voidAsyncResult1.failed()) {
                                        routingContext.fail(500, voidAsyncResult.cause());
                                        return;
                                    }
                                    routingContext.next();
                                });

                            });
                        });
                    } else {
                        routingContext.next();
                    }
                });
            }).handler(staticHandler);

            router.route("/*").failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
        };
        return routerConsumer;
    }
}
