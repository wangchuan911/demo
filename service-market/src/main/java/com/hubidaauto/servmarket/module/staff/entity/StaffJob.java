package com.hubidaauto.servmarket.module.staff.entity;

/**
 * @Classname StaffJob
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/27 13:30
 */
public class StaffJob {
    Long id, staffId, regionId, roleId;
    String name;

    public Long getId() {
        return id;
    }

    public StaffJob setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getStaffId() {
        return staffId;
    }

    public StaffJob setStaffId(Long staffId) {
        this.staffId = staffId;
        return this;
    }

    public Long getRegionId() {
        return regionId;
    }

    public StaffJob setRegionId(Long regionId) {
        this.regionId = regionId;
        return this;
    }

    public Long getRoleId() {
        return roleId;
    }

    public StaffJob setRoleId(Long roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getName() {
        return name;
    }

    public StaffJob setName(String name) {
        this.name = name;
        return this;
    }
}
