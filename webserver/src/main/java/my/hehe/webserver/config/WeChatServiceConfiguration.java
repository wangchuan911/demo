package my.hehe.webserver.config;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import my.hehe.webserver.common.JAXBUtils;
import my.hehe.webserver.common.encrypt.AesException;
import my.hehe.webserver.common.encrypt.WXBizMsgCrypt;
import my.hehe.webserver.entity.wechat.messeage.MesseageTypeValue;
import my.hehe.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import my.hehe.webserver.entity.wechat.messeage.response.ResponseMesseage;
import my.hehe.webserver.service.WeChatService;
import my.hehe.webserver.service.wechat.WeChatAsynService;
import my.hehe.webserver.vertx.annotation.VertxConfiguration;
import my.hehe.webserver.vertx.annotation.VertxRegister;
import my.hehe.webserver.vertx.verticle.AbstractCustomVerticle;
import my.hehe.webserver.vertx.verticle.StandaredVerticle;
import my.hehe.webserver.vertx.verticle.WorkerVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@VertxConfiguration
public class WeChatServiceConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WeChatServiceConfiguration.class);
    @Autowired
    @Qualifier("standaredVerticle")
    private StandaredVerticle standaredVerticle;
    @Autowired
    @Qualifier("weChatService")
    private WeChatService weChatService;

    @Value("${wechat.url.getToken}")
    private String WX_TOKEN_URL;
    @Value("${wechat.url.base}")
    private String WX_URL;

    @Value("${wechat.prop.appID}")
    private String appID;
    @Value("${wechat.prop.appsecret}")
    private String appsecret;
    @Value("${wechat.prop.token}")
    private String apptoken;

    @Value("${server.port}")
    private int SERVER_PORT;

    @Value("${wechat.prop.afterUpdateTokenTime}")
    private long TAKEN_UPDATE_TIME;

    private Long wechatAliveTimerId = null;

    WeChatAsynService weChatAsynService;

    @Bean
    public WXBizMsgCrypt getWXBizMsgCrypt(
            @Value("${wechat.prop.token}") String apptoken,
            @Value("${wechat.prop.key}") String appsecret,
            @Value("${wechat.prop.appID}") String appID) throws AesException {
        return new WXBizMsgCrypt(apptoken, appsecret, appID);
    }

    @Autowired
    private WXBizMsgCrypt wxBizMsgCrypt;

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncService() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            WeChatAsynService.create(vertx1);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            weChatAsynService = WeChatAsynService.createProxy(vertx1);


            final String key = "WX.TOKEN";
            final String URL_TOCKEN_LOCK = key + ".LOCK";
            final String URL_TOCKEN_UPDATE = key + ".UPDATE";
            final String URL_REQUSET = WX_URL + WX_TOKEN_URL;


            EventBus eventBus = vertx1.eventBus();
            WebClient webClient = WebClient.create(vertx1);
            SharedData sharedData = vertx1.sharedData();

            Handler<Long> longHandler = aLong -> {
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        webClient.getAbs(URL_REQUSET)
                                .addQueryParam("grant_type", "client_credential")
                                .addQueryParam("appid", appID)
                                .addQueryParam("secret", appsecret)
                                .send(httpResponseAsyncResult -> {
                                    try {
                                        if (httpResponseAsyncResult.succeeded()) {
                                            HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                            eventBus.publish(URL_TOCKEN_UPDATE, httpResponse.body().toJsonObject());
                                        } else {
                                            httpResponseAsyncResult.cause().printStackTrace();
                                        }
                                    } finally {
                                        Lock lock = lockAsyncResult.result();
                                        vertx1.setTimer(30 * 1000, aLong1 -> {
                                            lock.release();
                                        });
                                    }
                                });
                    } else {
                        logger.info(lockAsyncResult.cause().getMessage());
                    }
                });
            };

            eventBus.consumer(URL_TOCKEN_UPDATE).handler(objectMessage -> {
                JsonObject tokenJson = (JsonObject) objectMessage.body();
                if (tokenJson.getInteger("errcode") != null) {
                    logger.info("errcode:" + tokenJson.getInteger("errcode"));
                    logger.info("errmsg:" + tokenJson.getString("errmsg"));
                } else {
                    logger.info("Token:" + tokenJson.getString("access_token") + "[" + tokenJson.getLong("expires_in") + "]");
                }
                if (wechatAliveTimerId == null || vertx1.cancelTimer(wechatAliveTimerId)) {
                    wechatAliveTimerId = vertx1.setTimer(TAKEN_UPDATE_TIME * 1000, longHandler);
                }
            });
            longHandler.handle(null);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping() {
        Consumer<Router> routerConsumer = router -> {
            router.get("/wx").handler(routingContext -> {
                HttpServerRequest httpServerRequest = routingContext.request();
                String signature = httpServerRequest.getParam("signature");
                String timestamp = httpServerRequest.getParam("timestamp");
                String nonce = httpServerRequest.getParam("nonce");
                String echostr = httpServerRequest.getParam("echostr");
                if (StringUtils.isEmpty(signature)
                        || StringUtils.isEmpty(timestamp)
                        || StringUtils.isEmpty(nonce)
                        || StringUtils.isEmpty(echostr)) {
                    routingContext.response().end("interl server error");
                    return;
                }
                try {
                    routingContext.response().end(wxBizMsgCrypt.verifyUrl2(signature, timestamp, nonce, echostr));
                } catch (Exception e) {
                    routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                }
            });

            router.post("/wx").handler(routingContext -> {
                routingContext.response().setChunked(true);
                //解密
                try {
                    HttpServerRequest httpServerRequest = routingContext.request();
                    String signature = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_MSG_SIGNATURE);
                    String timeStamp = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_TIMESTAMP);
                    String nonce = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_NONCE);

                    if (StringUtils.isEmpty(timeStamp) && StringUtils.isEmpty(nonce)) {
                        return;
                    }

                    Buffer requestbuffer = routingContext.getBody();
                    requestbuffer = Buffer.buffer(wxBizMsgCrypt.decryptMsg(signature, timeStamp, nonce, requestbuffer.toString()));
                    routingContext.setBody(requestbuffer);

                } catch (AesException e) {
                    e.printStackTrace();
                } finally {
                    routingContext.next();
                }
            }).handler(routingContext -> {
                Buffer requestbuffer = routingContext.getBody();
                /*try {
                    //报文转对象
                    RequestMesseageBody requestMesseageBody = JAXBUtils.fromXML(requestbuffer.toString(), RequestMesseageBody.class);
                    //处理数据
                    ResponseMesseage responseMesseage = weChatService.receive(requestMesseageBody);
                    //报文转对象
                    requestbuffer = Buffer.buffer(JAXBUtils.toXML(responseMesseage));
                    routingContext.setBody(requestbuffer);
                    routingContext.next();
                } catch (Exception e) {
                    e.printStackTrace();
                    routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                    return;
                }*/

                weChatAsynService.receive(requestbuffer.toString(), stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        Buffer buffer = Buffer.buffer(stringAsyncResult.result());
                        routingContext.setBody(buffer);
                        routingContext.next();
                    } else {
                        stringAsyncResult.cause().printStackTrace();
                        routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                    }
                });

            }).handler(routingContext -> {
                Buffer requestbuffer = routingContext.getBody();
                try {

                    HttpServerRequest httpServerRequest = routingContext.request();
                    String timeStamp = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_TIMESTAMP);
                    String nonce = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_NONCE);


                    if (StringUtils.isEmpty(timeStamp) || StringUtils.isEmpty(nonce)) {
                        return;
                    }

                    requestbuffer = Buffer.buffer(wxBizMsgCrypt.encryptMsg(requestbuffer.toString(), timeStamp, nonce));
                } catch (AesException e) {
                    e.printStackTrace();
                } finally {
                    routingContext.response().end(requestbuffer);
                }
            }).failureHandler(routingContext -> {
                routingContext.response().end(MesseageTypeValue.MSG_REPLY);
            });
            logger.info("inital request mapping: /wx");
        };
        return routerConsumer;
    }

}

