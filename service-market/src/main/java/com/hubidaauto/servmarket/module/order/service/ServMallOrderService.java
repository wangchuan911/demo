package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.service.FlowService;

/**
 * @Classname ServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:13
 */
@Service
@DS("shop")
public class ServMallOrderService {
    static long TEMPLATE_ID = 1L;
    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    public String newOrder(OrderCondition a) {
        System.out.println(Thread.currentThread());
        Flow flow = new Flow();
        flow.setTemplateId(TEMPLATE_ID);
        this.flowService.flow(flow);
        return JSONObject.toJSONString(a);
    }
}
