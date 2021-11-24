package com.hubidaauto.servmarket.module.order.entity;

import java.sql.Timestamp;

public class ServiceClassOrderCondition extends OrderCondition<ServiceClassOrderVO> {
    Form form;

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public static class Form {
        String[] time;
        Integer timeCost;
        Long timeCostUnit;
        Long[] addedValue;
        Integer count;
        String remark;
        Long addressId, price, itemTypeId, regionId, typeId;

        public Long[] getAddedValue() {
            return addedValue;
        }

        public void setAddedValue(Long[] addedValue) {
            this.addedValue = addedValue;
        }

        public String[] getTime() {
            return time;
        }

        public void setTime(String[] time) {
            this.time = time;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Long getPrice() {
            return price;
        }

        public void setPrice(Long price) {
            this.price = price;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Long getAddressId() {
            return addressId;
        }

        public void setAddressId(Long addressId) {
            this.addressId = addressId;
        }

        public Long getItemTypeId() {
            return itemTypeId;
        }

        public void setItemTypeId(Long itemTypeId) {
            this.itemTypeId = itemTypeId;
        }

        public Long getRegionId() {
            return regionId;
        }

        public void setRegionId(Long regionId) {
            this.regionId = regionId;
        }

        public Integer getTimeCost() {
            return timeCost;
        }

        public void setTimeCost(Integer timeCost) {
            this.timeCost = timeCost;
        }

        public Long getTimeCostUnit() {
            return timeCostUnit;
        }

        public void setTimeCostUnit(Long timeCostUnit) {
            this.timeCostUnit = timeCostUnit;
        }

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }
    }
}


