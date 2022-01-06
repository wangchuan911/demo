package com.hubidaauto.servmarket.module.order.model;

import com.hubidaauto.servmarket.module.flow.enums.ServiceContent;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.entity.DetailVO;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;

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

    OrderVO getOrder(Long orderId);

    WorkOrderVO getWorkOrder(W workOrderCondition);

    List<WorkOrderVO> getWorkOrders(W workOrderCondition);

    List<ServiceContent> getServices(OrderVO orderVO);

    List<DetailVO> orderDetail(Long orderId);

    void dismiss(Long orderId);

    void modifyOrder(O condition);

    void destroy(Long orderId);


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
