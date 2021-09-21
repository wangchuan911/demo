package com.hubidaauto.servmarket.module.comment.entity;

/**
 * @Classname OrderCommentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/17 23:15
 */
public class OrderCommentVO extends CommentVO {
    Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
