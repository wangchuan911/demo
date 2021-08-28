package com.hubidaauto.servmarket.module.staff.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;
import com.hubidaauto.servmarket.common.entity.Page;

/**
 * @Classname StaffCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 09:47
 */
public class StaffCondition {
    Long orderId, staffId, roleId;
    boolean isPage;
    Page page;

    public StaffCondition setOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public StaffCondition setStaffId(Long staffId) {
        this.staffId = staffId;
        return this;
    }

    public Long getRoleId() {
        return roleId;
    }

    public StaffCondition setRoleId(Long roleId) {
        this.roleId = roleId;
        return this;
    }

    public boolean isPage() {
        return isPage;
    }

    public void setPage(boolean page) {
        isPage = page;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public StaffCondition page(int num) {
        if (this.page == null)
            this.page = new Page(num);
        return this;
    }

    public StaffCondition page(int num, int size) {
        if (this.page == null) {
            page = new Page(num, size);
        }
        return this;
    }
}
