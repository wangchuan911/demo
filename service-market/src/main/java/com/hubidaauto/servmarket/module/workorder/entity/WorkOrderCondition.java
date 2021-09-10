package com.hubidaauto.servmarket.module.workorder.entity;

/**
 * @Classname WorkOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 18:55
 */
public class WorkOrderCondition  {
    Long streamId, classId, orderId,
            id, staffId;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
}
