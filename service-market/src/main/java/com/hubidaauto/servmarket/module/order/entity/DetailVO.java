package com.hubidaauto.servmarket.module.order.entity;

/**
 * @Classname DetailVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/16 10:55
 */
public class DetailVO {
    String name;
    Object value;

    public DetailVO(String name, Object value) {
        this.value = value;
        this.name = name;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
