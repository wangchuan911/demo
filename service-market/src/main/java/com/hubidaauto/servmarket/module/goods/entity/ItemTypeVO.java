package com.hubidaauto.servmarket.module.goods.entity;

/**
 * @Classname ItemTypeVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/5 20:00
 */
public class ItemTypeVO {
    Long id, itemId, price, img, measurementUnit;
    Integer measurement;
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

    public Long getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(Long measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public Integer getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Integer measurement) {
        this.measurement = measurement;
    }
}
