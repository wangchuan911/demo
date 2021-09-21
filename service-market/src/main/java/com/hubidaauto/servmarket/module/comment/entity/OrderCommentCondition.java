package com.hubidaauto.servmarket.module.comment.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;

import java.util.Arrays;

/**
 * @Classname OrderCommentCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/21 15:10
 */
public class OrderCommentCondition extends AbstractBaseCondition<Long, OrderCommentVO> {
    final static String[] QUERY = {"LASTEST"};
    Long orderId, itemTypeId, itemId;
    String query;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        if (Arrays.stream(QUERY).anyMatch(s -> s.equals(query)))
            this.query = query;
        else
            throw new RuntimeException("no suppert!");
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
