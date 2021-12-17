package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;

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
    ItemTypeVO itemType;
    ItemVO item;

    public OverTimeOrderVO() {
    }

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

    public ItemTypeVO getItemType() {
        return itemType;
    }

    public void setItemType(ItemTypeVO itemType) {
        this.itemType = itemType;
    }

    public ItemVO getItem() {
        return item;
    }

    public void setItem(ItemVO item) {
        this.item = item;
    }
}
