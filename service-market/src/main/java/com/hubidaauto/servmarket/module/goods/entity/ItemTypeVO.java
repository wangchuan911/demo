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

    public ItemTypeVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getItemId() {
        return itemId;
    }

    public ItemTypeVO setItemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public Long getPrice() {
        return price;
    }

    public ItemTypeVO setPrice(Long price) {
        this.price = price;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public ItemTypeVO setDesc(String desc) {
        this.desc = desc;
        return this;
    }

}
