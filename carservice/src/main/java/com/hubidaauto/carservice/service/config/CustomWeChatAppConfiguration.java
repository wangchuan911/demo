package com.hubidaauto.carservice.service.config;

import com.hubidaauto.carservice.service.dao.TacheDao;
import com.hubidaauto.carservice.service.entity.OrderVO;
import com.hubidaauto.carservice.service.entity.TacheVO;
import com.hubidaauto.carservice.service.service.OperationService;
import com.hubidaauto.carservice.service.service.OrderService;
import com.hubidaauto.carservice.service.service.UserService;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.CommonConst;
import org.welisdoon.webserver.common.JAXBUtils;
import org.welisdoon.webserver.common.WechatAsyncMeassger;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PrePayResponseMesseage;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@ConfigurationProperties("wechat-app-hubida")
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
public class CustomWeChatAppConfiguration extends AbstractWechatConfiguration {
    //    final static String REQUEST_NAME = "requestService";
    private static final Logger logger = LoggerFactory.getLogger(CustomWeChatAppConfiguration.class);

    private int orderCycleTime;

    public Integer getOrderCycleTime() {
        return orderCycleTime;
    }

    public void setOrderCycleTime(int orderCycleTime) {
        this.orderCycleTime = orderCycleTime;
    }

    ICommonAsynService commonAsynService;

    OrderService orderService;
    UserService userService;
    OperationService operationService;


    WXBizMsgCrypt wxBizMsgCrypt;

    public static WechatAsyncMeassger wechatAsyncMeassger = null;

    @Value("${temp.filePath}")
    String staticPath;

    /*@VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            commonAsynService = ICommonAsynService.createProxy(vertx1);
            logger.info(String.format("create AsyncServiceProxy:%s", commonAsynService));
        };
        return vertxConsumer;
    }*/

    @PostConstruct
    void initValue() throws Throwable {
        TacheDao tacheDao = ApplicationContextProvider.getBean(TacheDao.class);
        orderService = ApplicationContextProvider.getBean(OrderService.class);
        userService = ApplicationContextProvider.getBean(UserService.class);
        operationService = ApplicationContextProvider.getBean(OperationService.class);
        CustomConst.TACHE.initTacheMapValue(tacheDao.listAll(new TacheVO().setTampalateId(1)));

        wxBizMsgCrypt = this.getWXBizMsgCrypt();

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
//        final String PATH_WX_APP = "/wxApp";
//        final String PATH_WX_APP_PAY = "/wxAppPay";
//        final String PATH_WX_APP_UPLOAD = "/imgUpd";
        final String URL_CODE_2_SESSION = this.getUrls().get("code2Session").toString();
        final String URL_UNIFIEDORDERON = this.getUrls().get("unifiedorder").toString();
        final String URL_SUBSCRIBESEND = this.getUrls().get("subscribeSend").toString();

        commonAsynService = ICommonAsynService.createProxy(vertx);
        logger.info(String.format("create AsyncServiceProxy:%s", commonAsynService));

//        final String PATH_PRROJECT = this.getClass().getResource("/").getPath();
        WebClient webClient = WebClient.create(vertx);

        this.initAccessTokenSyncTimer(vertx, webClient, objectMessage -> {
            JsonObject tokenJson = (JsonObject) objectMessage.body();
            if (tokenJson.getInteger("errcode") != null) {
                logger.info("errcode:" + tokenJson.getInteger("errcode"));
                logger.info("errmsg:" + tokenJson.getString("errmsg"));
            } else {
                String accessToken = tokenJson.getString("access_token");
                wechatAsyncMeassger.setToken(accessToken);
                logger.info("Token:" + accessToken + "[" + tokenJson.getLong("expires_in") + "]");
            }
        });

        wechatAsyncMeassger = new WechatAsyncMeassger(webClient, URL_SUBSCRIBESEND);

        Consumer<Router> routerConsumer = router -> {
            //get请求入口
            router.get(this.getPath().getApp()).handler(routingContext -> {
                routingContext.response().setChunked(true);
                MultiMap multiMap = routingContext.request().params();
                int code = Integer.parseInt(multiMap.get(CommonConst.WebParamsKeys.GET_CODE));
                String value = multiMap.get(CommonConst.WebParamsKeys.GET_VALUE_1);
                switch (code) {
                    //获取登陆信息
                    case CustomConst.OTHER.LOGIN:
                        webClient.getAbs(URL_CODE_2_SESSION + value)
                                .send(httpResponseAsyncResult -> {
                                    if (httpResponseAsyncResult.succeeded()) {
                                        HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                        JsonObject jsonObject = httpResponse.body().toJsonObject();
                                        String key = jsonObject.remove("session_key").toString();
                                        logger.info(key);
                                        String userId = jsonObject.getString("openid");
//                                        jsonObject.mergeIn((JsonObject) requestService.login(userId));
                                        jsonObject.mergeIn((JsonObject) userService.login(userId, key));
                                        routingContext.response().end(jsonObject.toBuffer());
                                    } else {
                                        routingContext.fail(httpResponseAsyncResult.cause());
                                    }
                                });
                        break;
                    //获取支付信息
                    case CustomConst.OTHER.PRE_PAY:
                        try {
                            int offset = 32;
                            String nonce = value.substring(0, offset);
                            String orderId = value.substring(offset, (offset = value.indexOf('.', offset)));
                            String timeStamp = value.substring(++offset, (offset = value.indexOf('.', offset)));
                            String custId = value.substring(++offset);

                            OrderVO orderVo = (OrderVO) orderService
                                    .handle(CustomConst.GET, Map.of("orderId", orderId, "custId", custId));
                            Buffer buffer = Buffer.buffer(JAXBUtils.toXML(new PrePayRequsetMesseage()
                                    .setAppId(this.getAppID())
                                    .setMchId(this.getMchId())
                                    .setNonceStr(nonce)
                                    .setBody(String.format("%s-服务费用结算:\n定单编号：%s\n金额%s", this.getAppName(), orderVo.getOrderCode(), orderVo.getCost()))
                                    .setOutTradeNo(orderVo.getOrderCode())
                                    .setTotalFee((int) (orderVo.getCost() * 100))
                                    .setSpbillCreateIp(this.getNetIp())
                                    .setNotifyUrl(this.getAddress() + this.getPath().getPay())
                                    .setTradeType("JSAPI")
                                    .setOpenid(orderVo.getCustId())
                                    .setSign(this.getMchKey())
                            ));
                            webClient.postAbs(URL_UNIFIEDORDERON)
                                    .sendBuffer(buffer, httpResponseAsyncResult -> {
                                        if (httpResponseAsyncResult.succeeded()) {
                                            JsonObject resultBodyJson = new JsonObject();
                                            try {
                                                PrePayResponseMesseage prePayResponseMesseage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), PrePayResponseMesseage.class);
                                                System.out.println(prePayResponseMesseage);
                                                if (!CommonConst.WeChatPubValues.SUCCESS.equals(prePayResponseMesseage.getResultCode())) {
                                                    resultBodyJson
                                                            .put("error", String.format("支付失败:%s[%s]",
                                                                    prePayResponseMesseage.getReturnMsg(),
                                                                    prePayResponseMesseage.getErrCode()));
                                                } else if (!CommonConst.WeChatPubValues.SUCCESS.equals(prePayResponseMesseage.getReturnCode())) {
                                                    resultBodyJson
                                                            .put("error", String.format("支付失败:%s[%s]",
                                                                    prePayResponseMesseage.getErrCodeDes(),
                                                                    prePayResponseMesseage.getErrCode()));
                                                } else {
                                                    String sign = String.format("appId=%s&nonceStr=%s&package=prepay_id=%s&signType=MD5&timeStamp=%s&key=%s"
                                                            , this.getAppID()
                                                            , nonce
                                                            , prePayResponseMesseage.getPrepayId()
                                                            , timeStamp
                                                            , this.getMchKey());
                                                    String prePayId = prePayResponseMesseage.getPrepayId();
                                                    resultBodyJson
                                                            .put("sign", DigestUtils.md5Hex(sign))
                                                            .put("prePayId", prePayId);
                                                }
                                                routingContext.response()
                                                        .end(resultBodyJson.toBuffer());
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

            //支付信息微信回调
            router.post(this.getPath().getPay()).handler(routingContext -> {
                routingContext.response().setChunked(true);
                try {
                    PayBillRequsetMesseage payBillRequsetMesseage = JAXBUtils.fromXML(routingContext.getBodyAsString(), PayBillRequsetMesseage.class);
                    OrderVO orderVO = new OrderVO()
                            .setOrderCode(payBillRequsetMesseage.getOutTradeNo())
                            .setCustId(payBillRequsetMesseage.getOpenId());
                    orderVO = (OrderVO) orderService
                            .handle(CustomConst.GET,
                                    JsonObject.mapFrom(orderVO).getMap());
                    PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
                    String code;
                    String msg;
                    if (orderVO.getOrderId() != null) {
                        code = "SUCCESS";
                        msg = "OK";
                        if (orderVO.getOrderState() >= 0) {
                            orderService.handle(CustomConst.MODIFY, new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
                        }
                    } else {
                        code = "FAIL";
                        msg = "定单不存在";
                    }
                    payBillResponseMesseage.setReturnCode(code);
                    payBillResponseMesseage.setReturnMsg(msg);
                    routingContext.response()
                            .end(Buffer.buffer(JAXBUtils.toXML(payBillResponseMesseage)));
                } catch (JAXBException e) {
                    e.printStackTrace();
                    routingContext.fail(e);
                }

            });
            //请求总入口
            router.post(this.getPath().getApp()).handler(routingContext -> {
                routingContext.response().setChunked(true);
                Requset requset = Requset.newInstance(routingContext);
                commonAsynService.requsetCall(requset, stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        routingContext.response().end(stringAsyncResult.result().toJson().toBuffer());
                    } else {
                        routingContext.fail(500, stringAsyncResult.cause());
                    }
                });

            });
            logger.info("inital request mapping: " + this.getPath().getApp());

            StaticHandler staticHandler = StaticHandler.create()
                    .setAllowRootFileSystemAccess(true)
                    .setWebRoot(staticPath);
            staticHandler.setAlwaysAsyncFS(true);
            staticHandler.setCachingEnabled(false);
//            staticHandler.setDirectoryListing(true);
//            staticHandler.setFilesReadOnly(false);
            //图片服务
            router.get("/pic/*").handler(routingContext -> {
                HttpServerRequest httpServerRequest = routingContext.request();
                String fileName = Utils.pathOffset(httpServerRequest.path(), routingContext);
                String file = staticPath + fileName;
                FileSystem fileSystem = routingContext.vertx().fileSystem();
                fileSystem.exists(file, booleanAsyncResult -> {
                    if (booleanAsyncResult.succeeded() && !booleanAsyncResult.result()) {
                        Requset requset = new Requset()
                                .setService("pictureService")
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


            router.get(this.getPath().getPush()).handler(this::wechatMsgCheck);

            router.route("/*").failureHandler(routingContext -> {
                routingContext.response().end(routingContext.failure().toString());
            });
        };
        return routerConsumer;
    }
}
