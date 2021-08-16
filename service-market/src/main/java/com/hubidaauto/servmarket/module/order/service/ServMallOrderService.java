package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import org.springframework.stereotype.Service;

/**
 * @Classname ServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:13
 */
@Service
@DS("shop")
public class ServMallOrderService {
    public String hehe(OrderCondition a) {
        System.out.println(Thread.currentThread());
        return JSONObject.toJSONString(a);
    }
}
