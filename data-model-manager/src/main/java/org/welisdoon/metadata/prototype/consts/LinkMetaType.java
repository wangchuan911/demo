package org.welisdoon.metadata.prototype.consts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * @Classname TableMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:11
 */
public enum LinkMetaType implements IMetaType {
    UNKNOWN(Long.MIN_VALUE, "未知"),
    PrimaryKey(3000, "主键"),
    ForeignKey(3001, "外键"),
    ObjectLinkSingleLineTable(3002, "对象与单行表的关系"),
    ObjectLinkMultiLineTable(3004, "对象与多行表的关系"),
    HeaderLinkAttributes(3005, "数据标题与属性的关系"),
    ObjToConstructor(3006, "对象构造定义"),
    ObjFromSourceDataToConstructor(3007, "对象构造定义(数据库)"),
    ObjFromTextDataToConstructor(3008, "对象构造定义(文本数据)"),
    Equal(3200, "等于"),
    NotEqual(3201, "不等于"),
    GreatThan(3202, "大于"),
    LessThan(3203, "小于"),
    OR(3204, "或"),
    AND(3205, "与");

    long id;
    String name;

    LinkMetaType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static LinkMetaType getInstance(long id) {
        return Arrays.stream(values()).filter(linkMetaType -> linkMetaType.id == id).findFirst().orElse(UNKNOWN);
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

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Meta
    public @interface LinkHandle {
        LinkMetaType value();
    }
}
