package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.ServiceClassOrderVO;
import com.hubidaauto.servmarket.module.order.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.order.entity.WorkOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.intf.FlowEvent;

/**
 * @Classname ServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:13
 */
@Service
@DS("shop")
@Transactional(rollbackFor = Throwable.class)
@OrderClass(id = 1000L)
public class ServiceClassOrderService implements FlowEvent, IOrderService<ServiceClassOrderVO, WorkOrderVO> {
    static long TEMPLATE_ID = 1L;
    FlowProxyService flowService;

    @Autowired
    public void setFlowService(FlowProxyService flowService) {
        this.flowService = flowService;
    }


    @Override
    @DS("flow")
    public void order(OrderCondition<ServiceClassOrderVO> condition) {
        System.out.println(Thread.currentThread());
        Flow flow = new Flow();
        flow.setTemplateId(TEMPLATE_ID);
        flow.setFunctionId(5L);
        this.flowService.flow(flow);
    }


    @Override
    public void start(OrderCondition<ServiceClassOrderVO> condition) {

        this.flowService.start(condition.getFlowId());
    }


    @Override
    public void workOrder(WorkOrderCondition<WorkOrderVO> workOrderCondition) {

        this.flowService.stream(workOrderCondition.getStreamId());
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
