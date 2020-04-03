package org.welisdoon.webserver.config;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.welisdoon.webserver.common.encrypt.AesException;
import org.welisdoon.webserver.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.webserver.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@VertxConfiguration
@ConfigurationProperties("wechat")
@Configuration
@ConditionalOnProperty(prefix = "wechat", name = "appID")
public class WeChatServiceConfiguration extends AbstractWechatConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WeChatServiceConfiguration.class);

    private Long wechatAliveTimerId = null;

    ICommonAsynService commonAsynService;

    private WXBizMsgCrypt wxBizMsgCrypt;

    @PostConstruct
    void initValue() throws Throwable {
        wxBizMsgCrypt = this.getWXBizMsgCrypt();
    }

    /*@VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            commonAsynService = ICommonAsynService.createProxy(vertx1);


            final String key = "WX.TOKEN";
            final String URL_TOCKEN_LOCK = key + ".LOCK";
            final String URL_TOCKEN_UPDATE = key + ".UPDATE";
            final String URL_REQUSET = this.getUrls().get("base").toString() + this.getUrls().get("getToken").toString();


            EventBus eventBus = vertx1.eventBus();
            WebClient webClient = WebClient.create(vertx1);
            SharedData sharedData = vertx1.sharedData();

            Handler<Long> longHandler = aLong -> {
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        webClient.getAbs(URL_REQUSET)
                                .addQueryParam("grant_type", "client_credential")
                                .addQueryParam("appid", this.getAppID())
                                .addQueryParam("secret", this.getAppsecret())
                                .timeout(20000)
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
                *//*if (wechatAliveTimerId != null) {
                    vertx1.cancelTimer(wechatAliveTimerId);
                }
                wechatAliveTimerId = vertx1.setTimer(this.getAfterUpdateTokenTime() * 1000, longHandler);*//*
            });
            longHandler.handle(null);
            vertx1.setPeriodic(this.getAfterUpdateTokenTime() * 1000, longHandler);
        };
        return vertxConsumer;
    }*/

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
        commonAsynService = ICommonAsynService.createProxy(vertx);
        WebClient webClient = WebClient.create(vertx);

        this.initAccessTokenSyncTimer(vertx, webClient, objectMessage -> {
            JsonObject tokenJson = (JsonObject) objectMessage.body();
            if (tokenJson.getInteger("errcode") != null) {
                logger.info("errcode:" + tokenJson.getInteger("errcode"));
                logger.info("errmsg:" + tokenJson.getString("errmsg"));
            } else {
                logger.info("Token:" + tokenJson.getString("access_token") + "[" + tokenJson.getLong("expires_in") + "]");
            }
        });

        Consumer<Router> routerConsumer = router -> {
            router.get("/wx").handler(this::wechatMsgCheck);

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
                    ResponseMesseage responseMesseage = weChatService.wechatMsgReceive(requestMesseageBody);
                    //报文转对象
                    requestbuffer = Buffer.buffer(JAXBUtils.toXML(responseMesseage));
                    routingContext.setBody(requestbuffer);
                    routingContext.next();
                } catch (Exception e) {
                    e.printStackTrace();
                    routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                    return;
                }*/

                commonAsynService.wechatMsgReceive(requestbuffer.toString(), stringAsyncResult -> {
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

