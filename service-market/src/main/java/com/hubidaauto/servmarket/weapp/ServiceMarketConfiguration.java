package com.hubidaauto.servmarket.weapp;

import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.service.wechat.service.AbstractWeChatService;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.function.Consumer;

@Configuration
//@ConfigurationProperties("wechat-app-hubida")
@AutoConfigureAfter(CustomWeChatAppConfiguration.class)
@VertxConfiguration
public class ServiceMarketConfiguration {
    AbstractWechatConfiguration abstractWechatConfiguration;

    @Autowired
    public void setAbstractWechatConfiguration(CustomWeChatAppConfiguration abstractWechatConfiguration) {
        this.abstractWechatConfiguration = abstractWechatConfiguration;
    }


}
