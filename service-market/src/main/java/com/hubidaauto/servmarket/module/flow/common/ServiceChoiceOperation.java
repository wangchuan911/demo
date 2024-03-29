package com.hubidaauto.servmarket.module.flow.common;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.flow.enums.ServiceContent;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname ServiceChoice
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/24 00:52
 */
@Component
@Transactional(rollbackFor = Throwable.class)
public class ServiceChoiceOperation implements VirtualNode.OnInstantiated{

    FlowService flowService;

    BaseOrderDao baseOrderDao;

    BaseOrderService baseOrderService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setBaseOrderService(BaseOrderService baseOrderService) {
        this.baseOrderService = baseOrderService;
    }

    @Override
    @DS("shop")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Stream> apply(Stream templateStream) {
        List<Stream> list = new LinkedList();
        OrderVO orderVO = baseOrderDao.find(new OrderCondition<>().setFlowId(templateStream.getFlowId()));
        List<ServiceContent> services = baseOrderService.getServices(orderVO);
        JSONObject valueJson;
        for (int i = 0, len = services.size(); i < len; i++) {
            Stream stream = new Stream();
            stream.setFlow(templateStream.getFlow());
            stream.setSuperId(templateStream.getSuperId());
            stream.setFlowId(templateStream.getFlowId());
            stream.setSeq(i + 1);
            stream.setFunctionId(4L);
            stream.setNodeId(1L);
            stream.setName(templateStream.getName());
//            stream.setValueId(services.get(i).getId());
            FlowValue flowValue = new FlowValue().setFlowId(templateStream.getFlowId());
            valueJson = flowValue.jsonValue();
            valueJson.put("function", services.get(i).getId());
            stream.setValue((FlowValue) flowValue.saveJsonValue());
            list.add(stream);
        }
        return list;
    }

}
