package com.hubidaauto.servmarket.module.order.entity;

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
        Integer measurement;
        Long measurementUnit;
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

        public Integer getMeasurement() {
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

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }
    }
}


