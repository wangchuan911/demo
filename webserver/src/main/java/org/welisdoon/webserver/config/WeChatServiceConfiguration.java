package org.welisdoon.webserver.config;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.welisdoon.webserver.common.web.AsyncProxyUtils;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.webserver.vertx.verticle.WorkerVerticle;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@VertxConfiguration
@ConfigurationProperties("wechat")
@Configuration
@ConditionalOnProperty(prefix = "wechat", name = "appID")
public class WeChatServiceConfiguration extends AbstractWechatConfiguration {

    private Long wechatAliveTimerId = null;

    ICommonAsynService commonAsynService;

    @PostConstruct
    public void initValue() throws Throwable {
        super.initValue();
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

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncService() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            AsyncProxyUtils.createServiceBinder(vertx1, this.getAppID(), ICommonAsynService.class);
        };
        return vertxConsumer;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
        commonAsynService = AsyncProxyUtils.createServiceProxyBuilder(vertx, this.getAddress(), ICommonAsynService.class);
        setWechatAsyncMeassger(WebClient.create(vertx));

        this.initAccessTokenSyncTimer(vertx, objectMessage -> {
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

            router.post("/wx").handler(this::wechatDecryptMsg).handler(routingContext -> {
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

                commonAsynService.callService(new Requset()
                        .setService("weChatService")
                        .setBody(requestbuffer.toString())
                        .setMode(Requset.WECHAT), responseAsyncResult -> {
                    if (responseAsyncResult.succeeded()) {
                        Buffer buffer = Buffer.buffer(responseAsyncResult.result().getResult().toString());
                        routingContext.setBody(buffer);
                        routingContext.next();
                    } else {
                        logger.error(responseAsyncResult.cause().getMessage(), responseAsyncResult.cause());
                        routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                    }
                });

            }).handler(this::wechatEncryptMsg).failureHandler(routingContext -> {
                routingContext.response().end(MesseageTypeValue.MSG_REPLY);
            });
            logger.info("inital request mapping: /wx");
        };
        return routerConsumer;
    }

}

