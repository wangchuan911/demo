package com.hubidaauto.servmarket.module.order.entity;

/**
 * @Classname OverTimeOrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:38
 */
public class OverTimeOrderVO extends OrderVO {
    Long relaOrderId;
    Integer workLoad;
    Long itemTypeId, typeId;

    public OverTimeOrderVO(OverTimeOrderCondtion.Form form) {

        this.workLoad = form.workLoad;
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

    public Integer getWorkLoad() {
        return workLoad;
    }

    public void setWorkLoad(Integer workLoad) {
        this.workLoad = workLoad;
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
