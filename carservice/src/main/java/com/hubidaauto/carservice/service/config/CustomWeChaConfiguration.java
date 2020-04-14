package com.hubidaauto.carservice.service.config;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.WechatAsyncMeassger;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.common.encrypt.AesException;
import org.welisdoon.webserver.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@VertxConfiguration
@ConfigurationProperties("wechat-public-hubida")
@Configuration
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class CustomWeChaConfiguration extends AbstractWechatConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CustomWeChaConfiguration.class);

    private Long wechatAliveTimerId = null;

    ICommonAsynService commonAsynService;

    private WXBizMsgCrypt wxBizMsgCrypt;

    @PostConstruct
    void initValue() throws Throwable {
        wxBizMsgCrypt = this.getWXBizMsgCrypt();
    }

    public static WechatAsyncMeassger wechatAsyncMeassger = null;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {
        final String URL_SUBSCRIBESEND = this.getUrls().get("subscribeSend").toString();

        commonAsynService = ICommonAsynService.createProxy(vertx);
        logger.info(String.format("create AsyncServiceProxy:%s", commonAsynService));

        WebClient webClient = WebClient.create(vertx);

        this.initAccessTokenSyncTimer(vertx, webClient, objectMessage -> {
            this.tokenHandler(objectMessage, accessToken -> {
                wechatAsyncMeassger.setToken(accessToken);
            }, s -> {
            });
        });
        wechatAsyncMeassger = new WechatAsyncMeassger(webClient, URL_SUBSCRIBESEND);

        Consumer<Router> routerConsumer = router -> {
            router.get(this.getPath().getPush()).handler(this::wechatMsgCheck);

            router.post(this.getPath().getPush()).handler(this::wechatDecryptMsg)
                    .handler(routingContext -> {
                        Buffer requestbuffer = routingContext.getBody();
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

                    }).handler(this::wechatEncryptMsg).failureHandler(routingContext -> {
                routingContext.response().end(MesseageTypeValue.MSG_REPLY);
            });
            logger.info(String.format("inital request mapping: %s", this.getPath().getPush()));
        };
        return routerConsumer;
    }

}

