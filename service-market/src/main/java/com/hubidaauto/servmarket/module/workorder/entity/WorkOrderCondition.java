package com.hubidaauto.servmarket.module.workorder.entity;

/**
 * @Classname WorkOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 18:55
 */
public class WorkOrderCondition {
    Long streamId, classId, orderId,
            id, staffId;

    public Long getStreamId() {
        return streamId;
    }

    public WorkOrderCondition setStreamId(Long streamId) {
        this.streamId = streamId;
        return this;
    }

    public Long getClassId() {
        return classId;
    }

    public WorkOrderCondition setClassId(Long classId) {
        this.classId = classId;
        return this;
    }

    public Long getOrderId() {
        return orderId;
    }

    public WorkOrderCondition setOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public Long getId() {
        return id;
    }

    public WorkOrderCondition setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getStaffId() {
        return staffId;
    }

    public WorkOrderCondition setStaffId(Long staffId) {
        this.staffId = staffId;
        return this;
    }
}
