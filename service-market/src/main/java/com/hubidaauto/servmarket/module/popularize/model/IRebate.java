package com.hubidaauto.servmarket.module.popularize.model;

import com.hubidaauto.servmarket.module.order.entity.OrderVO;

/**
 * @Classname IRebate
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/26 16:42
 */
public interface IRebate {
    String CONFIG_GROUP = "REBATE";

    Integer rebate(OrderVO orderVO);
}
