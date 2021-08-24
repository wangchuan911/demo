package com.hubidaauto.servmarket.module.order.service;

import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import io.vertx.core.Future;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;

/**
 * @Classname IServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 17:04
 */
@VertxServiceProxy(targetClass = ServMallOrderService.class, address = "{wechat-app-hubida.appID}")
public interface IServMallOrderService {
    Future<String> newOrder(OrderCondition a);
}
