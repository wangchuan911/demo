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
    Column(101, "表字段", ObjectMetaType.Table),
    Attributes(201, "对象属性", ObjectMetaType.Object),
    Header(301, "搜索结果标题", ObjectMetaType.Search),
    Condition(302, "搜索条件", ObjectMetaType.Search),
    Field(401, "实例属性", ObjectMetaType.Instance);
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
