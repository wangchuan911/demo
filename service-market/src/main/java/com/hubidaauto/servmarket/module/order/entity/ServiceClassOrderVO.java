package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.module.goods.dao.AddedValueDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        this.bookTime = Timestamp.valueOf(LocalDateTime.parse(String.format("%04d-%02d-%02d %02d:00:00", Arrays.stream((form.time[0] + "-" + form.time[1]).split("-")).map(s -> Integer.valueOf(s)).toArray(Object[]::new)), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.workerNum = form.count;
        this.measurement = form.measurement;
        this.measurementUnit = form.measurementUnit;
        this.price = form.price;
        this.itemTypeId = form.itemTypeId;
        this.typeId = form.typeId;
        this.regionId = form.regionId;
        if (form.addedValue != null) {
            AddedValueDao addedValueDao = ApplicationContextProvider.getBean(AddedValueDao.class);
            this.setAddedValue(Arrays.stream(form.addedValue).map(addedValueId ->
                    addedValueDao.get(addedValueId).getContext()
            ).collect(Collectors.joining(" ")));
        }

    }

    Long itemTypeId, addressId, payTypeId, typeId, measurementUnit;
    String remark, addedValue;
    Timestamp createTime;
    Timestamp finishTime;
    Timestamp bookTime;
    Integer workerNum, measurement;
    ItemTypeVO itemType;
    ItemVO item;


    public long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public long getPayTypeId() {
        return payTypeId == null ? 0L : payTypeId;
    }

    public void setPayTypeId(Long payTypeId) {
        this.payTypeId = payTypeId;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public void setWorkerNum(Integer workerNum) {
        this.workerNum = workerNum;
    }

    public int getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Integer measurement) {
        this.measurement = measurement;
    }

    public Long getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(Long measurementUnit) {
        this.measurementUnit = measurementUnit;
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

    public String getAddedValue() {
        return addedValue;
    }

    public void setAddedValue(String addedValue) {
        this.addedValue = addedValue;
    }


}
