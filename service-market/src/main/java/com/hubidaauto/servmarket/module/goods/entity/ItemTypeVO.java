package com.hubidaauto.servmarket.module.goods.entity;

/**
 * @Classname ItemTypeVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/5 20:00
 */
public class ItemTypeVO {
    Long id, itemId, price, img, timeCostUnit;
    Integer timeCost;
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

    public Long getImg() {
        return img;
    }

    public void setImg(Long img) {
        this.img = img;
    }

    public Long getTimeCostUnit() {
        return timeCostUnit;
    }

    public void setTimeCostUnit(Long timeCostUnit) {
        this.timeCostUnit = timeCostUnit;
    }

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
    }
}
