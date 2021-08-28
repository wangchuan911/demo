package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.ServiceClassOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.flow.module.flow.service.FlowService;

import java.util.Map;

/**
 * @Classname ServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:13
 */
@Service
@DS("shop")
@Transactional(rollbackFor = Throwable.class)
public class ServiceClassOrderService implements FlowEvent {
    static long TEMPLATE_ID = 1L;
    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }


    @DS("flow")
    public String newOrder(Map<String, Object> map) {
        System.out.println(Thread.currentThread());
        Flow flow = new Flow();
        flow.setTemplateId(TEMPLATE_ID);
        flow.setFunctionId(5L);
        this.flowService.flow(flow);
        return JSONObject.toJSONString(flow);
    }

    @DS("flow")
    public String start(Long id) {
        return JSONObject.toJSONString(this.flowService.start(id));
    }

    @DS("flow")
    public String stream(Long id) {
        return JSONObject.toJSONString(this.flowService.stream(id));
    }

    @Override
    public void onPreCreate(Flow flow) {
        System.out.println("onPreCreate");
    }

    @Override
    public void onCreated(Flow flow, Stream stream) {
        System.out.println("onCreated");

    }

    @Override
    public void onFinished(Flow flow, Stream stream) {
        System.out.println("onFinished");

    }

    @Override
    public void onPreStart(Stream stream) {
        System.out.println("onPreStart");

    }

    @Override
    public void onStarted(Stream stream) {
        System.out.println("onStarted");

    }

    @Override
    public void onPreFinish(Stream stream) {
        System.out.println("onPreFinish");

    }

    @Override
    public void onFinished(Stream stream) {
        System.out.println("onFinished");

    }

    @Override
    public void onError(Flow flow, Stream stream, Throwable e) {
        System.out.println("onError");
        e.printStackTrace();

    }

    @Override
    public void onFlowStatus(Flow flow, Stream stream) {
        System.out.println("onFlowStatus");

    }

    @Override
    public void onStreamStatus(Flow flow, Stream stream) {
        System.out.println("onStreamStatus");

    }
}
