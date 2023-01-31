package com.hubidaauto.servmarket.module.flow.common;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;

/**
 * @Classname ToCustOperation
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/24 10:56
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class ToCustOperation implements VirtualNode.OnInstantiated{
    BaseOrderDao baseOrderDao;

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @DS("shop")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @Override
    public List<Stream> apply(Stream templateStream) {
        OrderVO orderVO = baseOrderDao.find(new OrderCondition<>().setFlowId(templateStream.getFlowId()));
        Stream stream = new Stream();

        stream.setFlow(templateStream.getFlow());
        stream.setFlowId(templateStream.getFlowId());
        stream.setSuperId(templateStream.getSuperId());
        stream.setSeq(templateStream.getSeq());
        stream.setValue(templateStream.getValue());
        stream.setFunctionId(templateStream.getFunctionId());
        FlowValue flowValue = stream.getValue() == null ? new FlowValue().setFlowId(templateStream.getFlowId()) : stream.getValue();

        stream.setValue(flowValue);

        FlowValue.FlowJSONValue valueJson = flowValue.jsonValue();
        valueJson.put("staffId", orderVO.getCustId());


        /*stream.setValueId(flowValue.getId());*/
        stream.setNodeId(6L);
        stream.setName(templateStream.getName());

        return List.of(stream);
    }

}
