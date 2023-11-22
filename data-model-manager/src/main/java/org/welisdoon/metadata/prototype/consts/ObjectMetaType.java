package org.welisdoon.metadata.prototype.consts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname TableMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:11
 */
public enum ObjectMetaType implements IMetaType {
    Table(1000, "表"),
    Object(1001, "对象"),
    Search(1002, "搜索");

    long id;
    String name;

    ObjectMetaType(long id, String name) {
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

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Meta
    public @interface MetaType {
        ObjectMetaType value();
    }
}
