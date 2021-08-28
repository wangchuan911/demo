package com.hubidaauto.servmarket.module.staff.entity;

import com.hubidaauto.servmarket.module.user.entity.AppUserVO;

/**
 * @Classname StaffVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 09:39
 */
public class StaffVO  extends AppUserVO {
    long roleId;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
}
