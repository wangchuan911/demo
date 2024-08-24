package org.welisdoon.metadata.prototype.consts;

import org.springframework.util.Assert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SqlToJoin(3010, "对象关联数据库的表", ObjToDataBase),
    SqlToSelect(3011, "对象关联的数据库字段", ObjToDataBase),
    SqlToJoinOfMultiDataRel(3012, "对象关联数据库的表(多对一)", SqlToJoin),
    SqlToJoinOfWeakRel(3013, "对象关联数据库的表(非强制关联)", SqlToJoin),
    SqlToJoinOfStrongRel(3014, "对象关联数据库的表(强制关联)", SqlToJoin),
    DataFuture(3015, "数据特征"),
    Equal(3200, "等于"),
    NotEqual(3201, "不等于"),
    GreatThan(3202, "大于"),
    LessThan(3203, "小于"),
    OR(3204, "或"),
    AND(3205, "与"),
    Contain(3206, "包含"),
    NotContain(3207, "包含"),
    Exists(3208, "存在"),
    NotExists(3209, "不存在"),
    ItemOfParent(3210, "由上级LINK决定处理方式"),
    Value(3211, "sql最小项"),
    Values(3212, "sql最小项集");

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

    public LinkMetaType getParent() {
        return parent;
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
        LinkMetaType[] value();
    }

    public static List<Long> getChildTypeId(long typeId) {
        Optional<LinkMetaType> optional = Arrays.stream(values()).filter(linkMetaType -> Objects.equals(linkMetaType.getId(), typeId)).findFirst();
        if (optional.isEmpty()) {
            return Arrays.asList(UNKNOWN.getId());
        }
        return
                Arrays.stream(values())
                        .filter(linkMetaType -> linkMetaType.parent != null && Objects.equals(linkMetaType.parent.getId(), typeId))
                        .map(LinkMetaType::getId)
                        .collect(Collectors.toList());
    }


    public boolean isMatched(final LinkMetaType value, Side side) {
        if (value == null) {
            return false;
        }
        if (this.equals(value)) {
            return true;
        }
        switch (side) {
            case Up:
                return this.getParent().isMatched(value, side);
            case Down:
                return Arrays.stream(values()).anyMatch(linkMetaType -> {
                    return this.equals(linkMetaType.getParent()) && linkMetaType.isMatched(value, side);
                });
            default:
                throw new UnsupportedOperationException();
        }
    }
}
