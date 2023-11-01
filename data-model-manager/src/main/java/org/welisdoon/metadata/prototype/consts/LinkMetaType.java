package org.welisdoon.metadata.prototype.consts;

import java.lang.annotation.*;

/**
 * @Classname TableMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:11
 */
public enum LinkMetaType implements IMetaType {

    PrimaryKey(102, "主键"),
    ForeignKey(105, "外键"),
    ObjectLinkSingleLineTable(202, "对象与单行表的关系"),
    ObjectLinkMultiLineTable(203, "对象与多行表的关系"),
    HeaderLinkAttributes(302, "数据标题与属性的关系");

    long id;
    String name;

    LinkMetaType(long id, String name) {
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
        LinkMetaType value();
    }
}
