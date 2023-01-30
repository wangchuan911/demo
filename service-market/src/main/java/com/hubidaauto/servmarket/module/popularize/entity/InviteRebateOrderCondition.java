package com.hubidaauto.servmarket.module.popularize.entity;

import com.hubidaauto.servmarket.common.entity.PageCondition;

/**
 * @Classname InviteOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 20:22
 */
public class InviteRebateOrderCondition extends PageCondition {
    Long inviteMan, payState;

    public Long getInviteMan() {
        return inviteMan;
    }

    public InviteRebateOrderCondition setInviteMan(Long inviteMan) {
        this.inviteMan = inviteMan;
        return this;
    }

    public Long getPayState() {
        return payState;
    }

    public InviteRebateOrderCondition setPayState(Long payState) {
        this.payState = payState;
        return this;
    }
}
