package com.hubidaauto.servmarket.module.workorder.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;

/**
 * @Classname WorkOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 18:55
 */
public class WorkOrderCondition<T extends WorkOrderVO> extends AbstractBaseCondition<Long, T> {
    Long streamId, classId, orderId,
            workOrderId;

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }
}
