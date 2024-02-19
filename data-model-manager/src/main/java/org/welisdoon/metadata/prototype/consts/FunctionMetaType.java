package org.welisdoon.metadata.prototype.consts;

import java.util.Arrays;

/**
 * @Classname FunctionMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:38
 */
public enum FunctionMetaType implements IMetaType {
    UNKNOWN(Long.MIN_VALUE, "未知"),
    Trigger(4000, "触发器"),
    Function(4001, "函数");

    long id;
    String name;

    FunctionMetaType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDesc() {
        return name;
    }

    public static FunctionMetaType getInstance(long id) {
        return Arrays.stream(values()).filter(linkMetaType -> linkMetaType.id == id).findFirst().orElse(UNKNOWN);
    }
}
