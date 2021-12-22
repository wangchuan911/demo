package com.hubidaauto.servmarket.module.order.entity;

/**
 * @Classname OverTimeOrderCondtion
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:24
 */
public class OverTimeOrderCondtion extends OrderCondition<OverTimeOrderVO> {
    Long relaOrderId;

    Form form;

    public Long getRelaOrderId() {
        return relaOrderId;
    }

    public void setRelaOrderId(Long relaOrderId) {
        this.relaOrderId = relaOrderId;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public static class Form {
        Integer workLoad;
        Long itemTypeId, typeId, relaOrderId;
        Integer price;

        public Integer getWorkLoad() {
            return workLoad;
        }

        public void setWorkLoad(Integer workLoad) {
            this.workLoad = workLoad;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
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

        public Long getRelaOrderId() {
            return relaOrderId;
        }

        public void setRelaOrderId(Long relaOrderId) {
            this.relaOrderId = relaOrderId;
        }
    }
}
