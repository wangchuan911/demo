package com.hubidaauto.servmarket.module.popularize.entity;

/**
 * @Classname InviteRebateCountVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/27 15:54
 */
public class InviteRebateCountVO {
    Long userId;
    Integer totalRebate, paidRebate;

    public Long getUserId() {
        return userId;
    }

    public InviteRebateCountVO setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Integer getTotalRebate() {
        return totalRebate;
    }

    public InviteRebateCountVO setTotalRebate(Integer totalRebate) {
        this.totalRebate = totalRebate;
        return this;
    }

    public Integer getPaidRebate() {
        return paidRebate;
    }

    public InviteRebateCountVO setPaidRebate(Integer paidRebate) {
        this.paidRebate = paidRebate;
        return this;
    }
}
