package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.module.goods.dao.AddedValueDao;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Classname OverTimeOrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:38
 */
public class OverTimeOrderVO extends OrderVO {
    Long relaOrderId;
    Integer timeCost;
    Long itemTypeId, typeId;

    public OverTimeOrderVO(OverTimeOrderCondtion.Form form) {

        this.timeCost = form.timeCost;
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

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
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
