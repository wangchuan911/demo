package com.hubidaauto.servmarket.weapp;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.service.wechat.service.AbstractWeChatService;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.function.Consumer;

@Configuration
@ConfigurationProperties("wechat-app-hubida")
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
public class ServiceMarketConfiguration extends AbstractWechatConfiguration {
    private int orderCycleTime;

    public Integer getOrderCycleTime() {
        return orderCycleTime;
    }

    public void setOrderCycleTime(int orderCycleTime) {
        this.orderCycleTime = orderCycleTime;
    }

    @Value("${temp.filePath}")
    String staticPath;

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> workerVerticle() {

        final String KEY = ApplicationContextProvider.getRealClass(this.getClass()).getName().toUpperCase();
        final String URL_TOCKEN_LOCK = KEY + ".LOCK";
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            SharedData sharedData = vertx1.sharedData();
            Handler<Long> longHandler = aLong -> {
                /*集群锁，防止重复处理和锁表*/
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        try {
//                            requestService.toBeContinue();
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
    public Consumer<Vertx> standaredverticle() {
        return vertx -> {
            WebClient webClient = WebClient.create(vertx);
            setWechatAsyncMeassger(webClient);
            initApiAsyncMeassger(vertx);

            this.initAccessTokenSyncTimer(vertx, objectMessage -> {
                this.updateAccessToken(this.getTokenFromMessage(objectMessage));
            });
        };
    }


    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> initVertxServiceProxy() {
        return (verix) -> {
            AsyncProxyUtils.createServiceBinder(verix, this.getAppID(), IVertxInvoker.class);
        };
    }

}
