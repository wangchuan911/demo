package com.hubidaauto.servmarket.module.order.annotation;

import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname OrderClass
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 17:43
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderClass {
    long[] id();

    Class<? extends OrderCondition<?>> orderConditionClass();

    Class<? extends WorkOrderCondition<?>> workOrderConditionClass();

}
