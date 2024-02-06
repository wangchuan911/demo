package org.welisdoon.metadata.prototype.consts;

import org.springframework.util.Assert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    ObjConstructor(3006, "对象构造定义"),
    ObjToDataBase(3007, "对象构造定义(数据库)", ObjConstructor),
    ObjToTextData(3008, "对象构造定义(文本数据)", ObjConstructor),
    ObjExtendObj(3009, "对象构造定义(对象继承)", ObjConstructor),
    SqlToFrom(3010, "对象对象关联数据库的表", ObjToDataBase),
    SqlToSelect(3011, "对象关联的数据库字段", ObjToDataBase),
    SQLToJoin(3012, "对象关联数据库的数据关系", ObjToDataBase),
    Equal(3200, "等于"),
    NotEqual(3201, "不等于"),
    GreatThan(3202, "大于"),
    LessThan(3203, "小于"),
    OR(3204, "或"),
    AND(3205, "与"),
    Contain(3206, "包含"),
    NotContain(3207, "包含"),
    Exists(3208, "存在"),
    NotExists(3209, "不存在");

    long id;
    String name;
    LinkMetaType parent;

    LinkMetaType(long id, String name) {
        this(id, name, null);
    }

    LinkMetaType(long id, String name, LinkMetaType parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    static {
        List<Long> error = Arrays.stream(values()).collect(Collectors.groupingBy(LinkMetaType::getId)).entrySet().stream().filter(longListEntry -> longListEntry.getValue().size() > 1).map(entry -> entry.getKey()).collect(Collectors.toList());
        Assert.isTrue(error.size() == 0, error.stream().map(Object::toString).collect(Collectors.joining(",")) + "重复！");
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
