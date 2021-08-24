package com.hubidaauto.servmarket.module.flow.common;

import com.hubidaauto.servmarket.module.order.consts.OrderFlowTemplate;
import com.hubidaauto.servmarket.module.order.dao.ServMallOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.flow.module.template.intf.VirtualNodeInitializer;

import java.util.List;

/**
 * @Classname SignInOperation
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/23 23:24
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class SignInOperation implements VirtualNodeInitializer {

    ServMallOrderDao orderDao;

    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Autowired
    public void setOrderDao(ServMallOrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public List<Stream> onInstantiated(Stream templateStream) {
        return List.of(templateStream);
    }

    @Override
    public void onStart(Stream currentStream) {
        Template template = new Template();
        template.setId(OrderFlowTemplate.SIGN_IN.getTemplateId());
        flowService.streamExpandByTemplate(template, currentStream);
    }
}
