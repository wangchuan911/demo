package org.welisdoon.metadata.prototype.consts;

import java.util.Arrays;

/**
 * @Classname KeyValueMetaType
 * @Description TODO
 * @Author Septem
 * @Date 14:44
 */
public enum KeyValueMetaType implements IMetaType {
    UNKNOWN(Long.MIN_VALUE, "未知"),
    ObjectValue(5000, "值(关联对象)"),
    AttributeValue(5001, "值(关联属性)"),
    InstanceValue(5001, "值(关联实例)");
    long id;
    String name;

    KeyValueMetaType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static KeyValueMetaType getInstance(long id) {
        return Arrays.stream(values()).filter(linkMetaType -> linkMetaType.id == id).findFirst().orElse(UNKNOWN);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getDesc() {
        return this.name;
    }
}
