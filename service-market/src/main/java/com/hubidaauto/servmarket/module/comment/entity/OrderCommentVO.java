package com.hubidaauto.servmarket.module.comment.entity;

/**
 * @Classname OrderCommentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/17 23:15
 */
public class OrderCommentVO extends CommentVO {
    Long orderId, itemTypeId, itemId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
