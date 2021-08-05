package com.hubidaauto.servmarket.module.goods.entity;

/**
 * @Classname ItemTypeVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/5 20:00
 */
public class ItemTypeVO {
    Long id, itemId, price;
    String desc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
