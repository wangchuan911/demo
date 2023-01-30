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

    public StaffTaskVO setStaffId(Long staffId) {
        this.staffId = staffId;
        return this;
    }

    public Long getOrderId() {
        return orderId;
    }

    public StaffTaskVO setOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public Timestamp getTaskTime() {
        return taskTime;
    }

    public StaffTaskVO setTaskTime(Timestamp taskTime) {
        this.taskTime = taskTime;
        return this;
    }
}
