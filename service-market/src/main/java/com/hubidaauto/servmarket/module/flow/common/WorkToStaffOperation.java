package com.hubidaauto.servmarket.module.flow.common;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.staff.dao.StaffDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Classname WorkToStaffOperation
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 09:37
 */
@Component
@Transactional(rollbackFor = Throwable.class)
public class WorkToStaffOperation implements VirtualNode.OnInstantiated{
    BaseOrderDao baseOrderDao;
    StaffDao staffDao;

    @Autowired
    public void setStaffDao(StaffDao staffDao) {
        this.staffDao = staffDao;
    }


    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }


    @Override
    @DS("shop")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Stream> apply(Stream templateStream) {
        OrderCondition condition = new OrderCondition();
        condition.setFlowId(templateStream.getFlowId());
        OrderVO orderVO = baseOrderDao.find(condition);
        List<StaffVO> staffs = staffDao.list(new StaffCondition().setOrderId(orderVO.getId()).setQuery("WORKER"));
        if (CollectionUtils.isEmpty(staffs)) {
            throw new RuntimeException("未指派人，请确认");
        }
        AtomicInteger integer = new AtomicInteger(1);
        return staffs.stream().map(staffVO -> {
            Stream stream = new Stream();
            stream.setFlow(templateStream.getFlow());
            stream.setSuperId(templateStream.getSuperId());
            stream.setFlowId(templateStream.getFlowId());
            stream.setSeq(integer.getAndAdd(1));
            FlowValue flowValue = new FlowValue().setFlowId(templateStream.getFlowId());
            if (templateStream.getValue() != null)
                flowValue.setValue(templateStream.getValue().getValue());

            stream.setValue(flowValue);

            FlowValue.FlowJSONValue valueJson = flowValue.jsonValue();
            valueJson.put("staffId", staffVO.getId());


            /*stream.setValueId(flowValue.getId());*/
            stream.setNodeId(6L);
            stream.setName(templateStream.getName());
            return stream;
        }).collect(Collectors.toList());
    }


}
