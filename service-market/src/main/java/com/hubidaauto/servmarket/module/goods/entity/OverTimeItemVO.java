package com.hubidaauto.servmarket.module.goods.entity;

/**
 * @Classname OverTimeItemVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/4 12:32
 */
public class OverTimeItemVO {
    Long itemId, overTimeItemId;


    public Long getItemId() {
        return itemId;
    }

    public OverTimeItemVO setItemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public Long getOverTimeItemId() {
        return overTimeItemId;
    }

    public OverTimeItemVO setOverTimeItemId(Long overTimeItemId) {
        this.overTimeItemId = overTimeItemId;
        return this;
    }
}
