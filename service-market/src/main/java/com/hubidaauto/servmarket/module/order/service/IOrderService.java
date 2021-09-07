package com.hubidaauto.servmarket.module.order.service;

import com.hubidaauto.carservice.wxapp.mall.common.AbstractAutoAssign;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @Classname OrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 17:32
 */
public interface IOrderService<O extends OrderCondition, W extends WorkOrderCondition> {

    void start(O condition);

    OrderVO order(O condition);

    void workOrder(W workOrderCondition);

    OrderVO get(Long id);


    /* static void start(final long typeId,String J) {
         ApplicationContextProvider
                 .getApplicationContext()
                 .getBeansWithAnnotation(OrderClass.class)
                 .entrySet()
                 .stream()
                 .map(Map.Entry::getValue)
                 .filter(o ->
                         ApplicationContextProvider.getRealClass(o.getClass()).getAnnotation(OrderClass.class).id() == typeId
                 ).findFirst().get();
         *//*Type type = ApplicationContextProvider.getRealClass(object.getClass()).getGenericSuperclass();
        if (type == null) return null;
        if (((ParameterizedType) type).getRawType() != AbstractAutoAssign.class)
            return null;
        type = ((ParameterizedType) type).getActualTypeArguments()[0];*//*
    }*/
    static OrderClass getClassMeta(Class<?> clz) {
        return ApplicationContextProvider
                .getRealClass(clz).getAnnotation(OrderClass.class);
    }
}
