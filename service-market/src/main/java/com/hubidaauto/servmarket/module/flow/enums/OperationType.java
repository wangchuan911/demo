package com.hubidaauto.servmarket.module.flow.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Classname OperationType
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/30 09:46
 */
public enum OperationType {
    SIGN_UP(-1), SERVICING(-2), DISPATCH(-3), UNKNOW(0);

    OperationType(long id) {
        this.id = id;
    }

    public final long id;

    public static OperationType getInstance(long id) {
        Optional<OperationType> optional = Arrays.stream(OperationType.values()).filter(operationType -> operationType.id == id).findFirst();
        return optional.isPresent() ? optional.get() : UNKNOW;
    }
}
