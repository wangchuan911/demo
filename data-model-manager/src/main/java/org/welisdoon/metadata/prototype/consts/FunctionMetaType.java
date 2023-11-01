package org.welisdoon.metadata.prototype.consts;

/**
 * @Classname FunctionMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:38
 */
public enum FunctionMetaType implements IMetaType {
    Trigger(103, "触发器"),
    Function(104, "函数");

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
}
