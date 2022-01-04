package com.hubidaauto.servmarket.module.order.entity;

import org.springframework.util.Assert;

import java.util.Arrays;

public class ServiceClassOrderCondition extends OrderCondition<ServiceClassOrderVO> {
    Form form;
    String modify;
    final static String[] MODIFYS = new String[]{"dispatching"};

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public static class Form {
        String[] time;
        Integer workLoad;
        Long workLoadUnit;
        Long[] addedValue;
        Integer count;
        String remark;
        Long addressId, itemTypeId, regionId, typeId;
        Integer price;

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

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
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

        public Integer getWorkLoad() {
            return workLoad;
        }

        public void setWorkLoad(Integer workLoad) {
            this.workLoad = workLoad;
        }

        public Long getWorkLoadUnit() {
            return workLoadUnit;
        }

        public void setWorkLoadUnit(Long workLoadUnit) {
            this.workLoadUnit = workLoadUnit;
        }

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }
    }

    public String getModify() {
        return modify;
    }

    public void setModify(String modify) {
        Assert.isTrue(Arrays.stream(MODIFYS).filter(s -> s.equals(modify)).findFirst().isPresent(),"");
        this.modify = modify;
    }
}


