package com.hubidaauto.servmarket.module.popularize.entity;

import com.hubidaauto.servmarket.common.entity.PageCondition;

/**
 * @Classname InviteOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 20:22
 */
public class InviteOrderCondition extends PageCondition {
    Long   inviteMan;

    public Long getInviteMan() {
        return inviteMan;
    }

    public void setInviteMan(Long inviteMan) {
        this.inviteMan = inviteMan;
    }
}
