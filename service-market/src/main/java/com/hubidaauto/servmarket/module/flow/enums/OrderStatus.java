package com.hubidaauto.servmarket.module.flow.enums;

/**
 * @Classname OrderStatus
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/30 22:07
 */
public enum OrderStatus {

    READY(10000), COMPLETE(10001), DISMISS(10002), PRE_PAY(10003), PAYING(10004), PAID(10005);

    long id;

    OrderStatus(long id) {
        this.id = id;
    }

    public long statusId() {
        return id;
    }
}
