package com.hubidaauto.servmarket.module.popularize.entity;

import com.hubidaauto.servmarket.module.order.entity.OrderVO;

/**
 * @Classname InviteOrder
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 20:03
 */
public class InviteRebateOrderVO extends OrderVO  {
    Long  payState, inviteMan;
    Integer rebate;



    public Long getPayState() {
        return payState;
    }

    public InviteRebateOrderVO setPayState(Long payState) {
        this.payState = payState;
        return this;
    }

    public Long getInviteMan() {
        return inviteMan;
    }

    public InviteRebateOrderVO setInviteMan(Long inviteMan) {
        this.inviteMan = inviteMan;
        return this;
    }

    public Integer getRebate() {
        return rebate;
    }

    public InviteRebateOrderVO setRebate(Integer rebate) {
        this.rebate = rebate;
        return this;
    }
}
