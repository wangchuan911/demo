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
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;

/**
 * @Classname SignInOperation
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/23 23:24
 */
@Service
@DS("shop")
@Transactional(rollbackFor = Throwable.class)
public class SignInOperation implements VirtualNode.OnStart {

    ServiceClassOrderDao orderDao;

    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Autowired
    public void setOrderDao(ServiceClassOrderDao orderDao) {
        this.orderDao = orderDao;
    }


    @Override
    public void apply(Stream currentStream) {
        Template template = new Template();
        template.setId(OrderFlowTemplate.SIGN_IN.getTemplateId());
        flowService.streamExpandByTemplate(template, currentStream);
    }
}
