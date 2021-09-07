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
        Boolean[] addedValue, time;
        Integer count;
        Timestamp date;
        String remark;
        Long addressId, price, itemTypeId;

        public Boolean[] getAddedValue() {
            return addedValue;
        }

        public void setAddedValue(Boolean[] addedValue) {
            this.addedValue = addedValue;
        }

        public Boolean[] getTime() {
            return time;
        }

        public void setTime(Boolean[] time) {
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

        public Timestamp getDate() {
            return date;
        }

        public void setDate(Timestamp date) {
            this.date = date;
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
    }
}


