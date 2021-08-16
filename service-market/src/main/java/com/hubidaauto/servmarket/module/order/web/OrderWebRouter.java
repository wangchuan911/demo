package com.hubidaauto.servmarket.module.order.web;

import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.IServMallOrderService;
import com.hubidaauto.servmarket.module.order.service.ServMallOrderService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname OrderWebRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/order")
public class OrderWebRouter {
    AbstractWechatConfiguration customWeChatAppConfiguration;
    AppUserDao appUserDao;
    IServMallOrderService iServMallOrderService;

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Autowired
    public void setAbstractWechatConfiguration(CustomWeChatAppConfiguration customWeChatAppConfiguration) {
        this.customWeChatAppConfiguration = customWeChatAppConfiguration;
    }

    @Autowired
    public void setiServMallOrderService(IServMallOrderService iServMallOrderService) {
        this.iServMallOrderService = iServMallOrderService;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/pay\\/(?<userId>\\d+)\\/(?<orderId>\\d+)\\/(?<nonce>\\w+)\\/(?<timeStamp>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void payment(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            AppUserVO userVO = appUserDao.get(Long.parseLong(routingContext.pathParam("userId")));
            WeChatPayOrder weChatPayOrder = new WeChatPayOrder()
                    .setId(routingContext.pathParam("orderId"))
                    .setUserId(userVO.getAppId())
                    .setNonce(routingContext.pathParam("nonce"))
                    .setTimeStamp(routingContext.pathParam("timeStamp"));
            this.customWeChatAppConfiguration.getWechatPrePayInfo(weChatPayOrder).onSuccess(jsonObject -> {
                routingContext.end(jsonObject.toBuffer());
            }).onFailure(throwable -> {
                routingContext.response().setStatusCode(500).end(throwable.getMessage());
            });
        });
    }

    @VertxRouter(path = "",
            method = "POST")
    public void newOder(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            iServMallOrderService.hehe((OrderCondition) new OrderCondition().setData(new OrderVO())).onSuccess(value -> {
                routingContext.end(value);
            }).onFailure(throwable -> {
                routingContext.end(throwable.getMessage());
            });
        });
    }


}
