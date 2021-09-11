package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;

import java.sql.Timestamp;

/**
 * @Classname ServiceClassOrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/26 00:01
 */
public class ServiceClassOrderVO extends OrderVO {
    public ServiceClassOrderVO() {
    }

    public ServiceClassOrderVO(ServiceClassOrderCondition.Form form) {
        this.addressId = form.addressId;
        this.remark = form.remark;
        this.bookTime = form.date;
        this.workerNum = form.count;
        this.workHour = (form.time[0] ? 4 : 0) + (form.time[1] ? 4 : 0);
        this.workTime = String.format("%s %s", form.time[0] ? "早上" : "", form.time[1] ? "下午" : "");
        this.price = form.price;
        this.itemTypeId = form.itemTypeId;
        this.regionId = form.regionId;
    }

    long itemTypeId, addressId, payTypeId, typeId;
    String remark, orderDesc, workTime;
    Timestamp createTime;
    Timestamp finishTime;
    Timestamp bookTime;
    int workerNum, workHour;
    ItemTypeVO itemType;
    ItemVO item;


    public long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public long getPayTypeId() {
        return payTypeId;
    }

    public void setPayTypeId(long payTypeId) {
        this.payTypeId = payTypeId;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public Timestamp getBookTime() {
        return bookTime;
    }

    public void setBookTime(Timestamp bookTime) {
        this.bookTime = bookTime;
    }

    public int getWorkerNum() {
        return workerNum;
    }

    public void setWorkerNum(int workerNum) {
        this.workerNum = workerNum;
    }

    public int getWorkHour() {
        return workHour;
    }

    public void setWorkHour(int workHour) {
        this.workHour = workHour;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
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
