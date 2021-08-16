package com.hubidaauto.carservice.officalaccount.config;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.common.web.Requset;
import org.welisdoon.web.common.web.intf.ICommonAsynService;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@VertxConfiguration
@ConfigurationProperties("wechat-public-hubida")
@Configuration
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class CustomWeChaConfiguration extends AbstractWechatConfiguration {

    private Long wechatAliveTimerId = null;

    ICommonAsynService commonAsynService;


    @Override
    @PostConstruct
    public void initValue() throws Throwable {
        super.initValue();
    }


    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {

        this.initAccessTokenSyncTimer(vertx, objectMessage -> {
            this.getWechatAsyncMeassger().setToken(this.getTokenFromMessage(objectMessage));
        });

        commonAsynService = AsyncProxyUtils.createServiceProxyBuilder(vertx, this.getAppID(), ICommonAsynService.class);

        this.setWechatAsyncMeassger(WebClient.create(vertx));


        Consumer<Router> routerConsumer = router -> {
            BodyHandler bodyHandler = BodyHandler.create();
            router.get(this.getPath().getPush()).handler(bodyHandler).handler(this::wechatMsgCheck);

            router.post(this.getPath().getPush()).handler(bodyHandler).handler(this::wechatDecryptMsg)
                    .handler(routingContext -> {
                        Buffer requestbuffer = routingContext.getBody();
                        commonAsynService.callService(new Requset()
                                .setService("customWeChatOfficalAccountService")
                                .setBody(requestbuffer.toString())
                                .setMode(Requset.WECHAT), stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                Buffer buffer = Buffer.buffer(stringAsyncResult.result().getResult().toString());
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

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncService() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            AsyncProxyUtils.createServiceBinder(vertx1, this.getAppID(), ICommonAsynService.class);
        };
        return vertxConsumer;
    }
}

