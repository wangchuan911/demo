package com.hubidaauto.servmarket.module.flow.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.consts.OrderFlowTemplate;
import com.hubidaauto.servmarket.module.order.dao.ServiceClassOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.flow.module.template.service.VirtualNodeService;

import java.util.List;

/**
 * @Classname AppointWorkOperation
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/23 23:35
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class AppointWorkOperation implements VirtualNodeService.VirtualNodeInitializer {
    ServiceClassOrderDao orderDao;

    @Autowired
    public void setOrderDao(ServiceClassOrderDao orderDao) {
        this.orderDao = orderDao;
    }

    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    @DS("shop")
    public List<Stream> onInstantiated(Stream templateStream) {
        return List.of(templateStream);
    }

    @Override
    public void onStart(Stream currentStream) {
        Template template = new Template();
        template.setId(OrderFlowTemplate.SERVICE_CHOICE.getTemplateId());
        flowService.streamExpandByTemplate(template, currentStream);
    }
}
