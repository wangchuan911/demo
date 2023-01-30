package com.hubidaauto.servmarket.module.flow.enums;

import java.util.Arrays;

/**
 * @Classname OrderStatus
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/30 22:07
 */
public enum OrderStatus {

    READY(10000), COMPLETE(10001), DISMISS(10002), PRE_PAY(10003), PAYING(10004),REFUNDING(10005),REFUNDED(10006);

    long id;

    OrderStatus(long id) {
        this.id = id;
    }

    public long statusId() {
        return id;
    }

    public static OrderStatus getInstance(long id) {
       return Arrays.stream(values()).filter(orderStatus -> orderStatus.id == id).findFirst().get();
    }
}
