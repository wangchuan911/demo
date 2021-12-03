package com.hubidaauto.servmarket.module.order.entity;

/**
 * @Classname OverTimeOrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:38
 */
public class OverTimeOrderVO extends OrderVO {
    Long relaOrderId;
    Integer measurement;
    Long itemTypeId, typeId;

    public OverTimeOrderVO(OverTimeOrderCondtion.Form form) {

        this.measurement = form.measurement;
        this.price = form.price;
        this.itemTypeId = form.itemTypeId;
        this.typeId = form.typeId;
    }

    public Long getRelaOrderId() {
        return relaOrderId;
    }

    public void setRelaOrderId(Long relaOrderId) {
        this.relaOrderId = relaOrderId;
    }

    public Integer getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Integer measurement) {
        this.measurement = measurement;
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
}
