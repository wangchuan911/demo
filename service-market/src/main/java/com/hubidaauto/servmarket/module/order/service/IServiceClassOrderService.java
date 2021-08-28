package com.hubidaauto.servmarket.module.order.service;

import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.ServiceClassOrderVO;
import io.vertx.core.Future;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;

import java.util.Map;

/**
 * @Classname IServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 17:04
 */
@VertxServiceProxy(targetClass = ServiceClassOrderService.class, address = "{wechat-app-hubida.appID}")
public interface IServiceClassOrderService {
    Future<String> newOrder(Map<String, Object> a);

    Future<String> start(Long flowId);

    Future<String> stream(Long flowId);

}
