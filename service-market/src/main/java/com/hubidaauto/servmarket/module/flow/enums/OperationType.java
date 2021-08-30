package com.hubidaauto.servmarket.module.flow.enums;

import java.util.Arrays;

/**
 * @Classname OperationType
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/30 09:46
 */
public enum OperationType {
    SIGN_UP(-1), SERVICING(-2);

    OperationType(long id) {
        this.id = id;
    }

    long id;

    public static OperationType getInstance(long id) {
        return Arrays.stream(OperationType.values()).filter(operationType -> operationType.id == id).findFirst().get();
    }
}
