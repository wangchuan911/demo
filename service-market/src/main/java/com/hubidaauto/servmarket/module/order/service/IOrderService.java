package com.hubidaauto.servmarket.module.order.service;

import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;

/**
 * @Classname OrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 17:32
 */
public interface IOrderService<O extends OrderVO, W extends WorkOrderVO> {
    void start(OrderCondition<O> condition);

    void order(OrderCondition<O> condition);

    void workOrder(WorkOrderCondition<W> workOrderCondition);

    public static void getInstance() {

    }
}
