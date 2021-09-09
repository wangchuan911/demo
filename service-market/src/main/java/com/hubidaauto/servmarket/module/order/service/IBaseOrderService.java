package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.entity.ServiceClassOrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import io.vertx.core.Future;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @Classname IServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 17:04
 */
@VertxServiceProxy(targetClass = BaseOrderService.class, address = "{wechat-app-hubida.appID}")
public interface IBaseOrderService {
    Future<OrderVO> order(String jsonText);

    Future<Void> start(String jsonText);

    Future<Void> workOrder(String jsonText);

    Future<List<OrderVO>> list(OrderCondition condition);

    Future<OrderVO> get(Long id);

    Future<WorkOrderVO> getWorkOrder(String jsonText);

    Future<List<WorkOrderVO>> getWorkOrders(String jsonText);


}
