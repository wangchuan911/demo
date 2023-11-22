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
public enum AttributeMetaType implements IMetaType {
    Column(2000, "表字段", ObjectMetaType.Table),
    Attributes(2001, "对象属性", ObjectMetaType.Object),
    Header(2002, "搜索结果标题", ObjectMetaType.Search),
    Condition(2003, "搜索条件", ObjectMetaType.Search);
    long id;
    String name;
    ObjectMetaType objectMetaType;

    AttributeMetaType(long id, String name, ObjectMetaType objectMetaType) {
        this.id = id;
        this.name = name;
        this.objectMetaType = objectMetaType;
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
        AttributeMetaType value();
    }
}
