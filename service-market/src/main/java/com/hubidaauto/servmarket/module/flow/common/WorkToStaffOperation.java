package com.hubidaauto.servmarket.module.flow.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.FlowProxyService;
import com.hubidaauto.servmarket.module.staff.dao.StaffDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
public class WorkToStaffOperation implements VirtualNode.VirtualNodeInitializer {
    BaseOrderDao baseOrderDao;
    StaffDao staffDao;
    FlowProxyService flowProxyService;

    @Autowired
    public void setStaffDao(StaffDao staffDao) {
        this.staffDao = staffDao;
    }


    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setFlowProxyService(FlowProxyService flowProxyService) {
        this.flowProxyService = flowProxyService;
    }




    @Override
    @DS("shop")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Stream> onInstantiated(Stream templateStream) {
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
            stream.setValueId(staffVO.getId());
            stream.setFunctionId(templateStream.getValueId());
            stream.setNodeId(6L);
            stream.setName(templateStream.getName());
            return stream;
        }).collect(Collectors.toList());
    }

    @Override
    public void onStart(Stream currentStream) {
        throw new RuntimeException("无效节点");
    }

}
