package com.hubidaauto.carservice.officalaccount.config;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.common.web.Requset;
import org.welisdoon.web.common.web.intf.ICommonAsynService;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.push.PublicTamplateMessage;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.Consumer;

@VertxConfiguration
@ConfigurationProperties("wechat-public-hubida")
@Configuration
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class CustomWeChaConfiguration extends AbstractWechatConfiguration {

    private Long wechatAliveTimerId = null;

    ICommonAsynService commonAsynService;
    OfficalAccoutUserDao officalAccoutUserDao;

    @Autowired
    public void setOfficalAccoutUserDao(OfficalAccoutUserDao officalAccoutUserDao) {
        this.officalAccoutUserDao = officalAccoutUserDao;
    }

    @Override
    @PostConstruct
    public void initValue() throws Throwable {
        super.initValue();
    }


    @VertxRegister(StandaredVerticle.class)
    public Consumer<Router> routeMapping(Vertx vertx) {

        this.initAccessTokenSyncTimer(vertx, objectMessage -> {
            this.setAccessToken(this.getTokenFromMessage(objectMessage));
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
            router.post(this.getPath().getPush() + "/p").handler(bodyHandler).handler(routingContext -> {
                try {
                    logger.info(routingContext.getBodyAsString());
                    JSONObject jsonObject = JSONObject.parseObject(routingContext.getBodyAsString());
                    send(jsonObject.getString("code"), jsonObject.getString("templateId"), jsonObject.getObject("params", Map.class));
                    routingContext.end("");
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    routingContext.response().setStatusCode(500).end(e.getMessage());
                }
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


    private void send(String unionId, String templateId, Map<String, Object> map) {
        UserVO userVO = officalAccoutUserDao.get(new UserVO().setUnionid(unionId).openData(false));
        if (userVO == null || StringUtils.isEmpty(userVO.openData(false).getUnionid()))
            throw new RuntimeException(userVO == null ? "没找到用户" : "用户没登陆过小程序");
        CustomWeChatAppConfiguration appConfiguration = AbstractWechatConfiguration
                .getConfig(CustomWeChatAppConfiguration.class);
        AbstractWechatConfiguration.getConfig(CustomWeChaConfiguration.class)
                .getWechatAsyncMeassger()
                .post(CommonConst.WecharUrlKeys.SUBSCRIBE_SEND,
                        new PublicTamplateMessage()
                                .setMiniprogram(new PublicTamplateMessage.MiniProgram()
                                        .setAppid(appConfiguration.getAppID())
                                        .setPagepath(appConfiguration.getPath().getAppIndex()))
                                .setTemplateId(templateId)
                                .addDatas(map.entrySet().toArray(Map.Entry[]::new))
                                .setTouser(userVO
                                        .getId()))
                .onSuccess(
                        bufferHttpResponse -> {
                            logger.info(bufferHttpResponse.body().toString());
                        })
                .onFailure(
                        throwable -> {
                            logger.error(throwable.getMessage(), throwable);
                        });
        logger.info("发送中");
    }
}

