package com.hubidaauto.servmarket.module.order.model;

import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.entity.OverTimeOrderVO;

/**
 * @Classname OverTimeEnable
 * @Description 可处理的加时的工单
 * @Author wang.zhidong
 * @Date 2021/11/30 00:25
 */
@FunctionalInterface
public interface IOverTimeOperationable {
    void overtime(OverTimeOrderVO overTimeOrder);
}
