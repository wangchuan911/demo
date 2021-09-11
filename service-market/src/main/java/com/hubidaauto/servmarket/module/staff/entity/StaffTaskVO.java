package com.hubidaauto.servmarket.module.staff.entity;

import java.sql.Timestamp;

/**
 * @Classname StaffTaskVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/11 15:07
 */
public class StaffTaskVO {
    Long staffId, orderId;
    Timestamp taskTime;

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Timestamp getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Timestamp taskTime) {
        this.taskTime = taskTime;
    }
}
