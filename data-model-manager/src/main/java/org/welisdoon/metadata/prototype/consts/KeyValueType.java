package org.welisdoon.metadata.prototype.consts;

import java.util.Arrays;

/**
 * @Classname KeyValueMetaType
 * @Description TODO
 * @Author Septem
 * @Date 14:44
 */
public enum KeyValueType {
    UNKNOWN(Long.MIN_VALUE, "未知"),
    Numeric(5000, "数字"),
    String(5001, "字符串"),
    Char(5002, "字符"),
    JSON(5003, "json格式数据"),
    Xml(5004, "xml格式数据"),
    Properties(5005, "properties格式数据"),
    Yml(5005, "yml格式数据"),
    Bigfile(5006, "大文件"),
    Binary(5007, "二进制");
    long id;
    String name;

    KeyValueType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static KeyValueType getInstance(long id) {
        return Arrays.stream(values()).filter(linkMetaType -> linkMetaType.id == id).findFirst().orElse(UNKNOWN);
    }

    public long getId() {
        return this.id;
    }

    public String getDesc() {
        return this.name;
    }
}
