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

/**
 * @Classname TableMetaType
 * @Description TODO
 * @Author Septem
 * @Date 10:11
 */
public enum LinkMetaType implements IMetaType {
    UNKNOWN(Long.MIN_VALUE, "未知"),
    Content(Long.MIN_VALUE + 1, "上下文"),
    /*ObjectLinkSingleLineTable(3002, "对象与单行表的关系"),
    ObjectLinkMultiLineTable(3004, "对象与多行表的关系"),
    HeaderLinkAttributes(3005, "数据标题与属性的关系"),*/
    ObjConstructor(3006, "对象构造定义"),

    ObjToDataBase(3007, "对象构造定义(数据库)", ObjConstructor),
    ObjToTextData(3008, "对象构造定义(文本数据)", ObjConstructor),
    ObjExtendObj(3009, "对象构造定义(对象继承)", ObjConstructor),

    SqlToJoin(3010, "对象关联数据库的表", ObjToDataBase),
    SqlToSelect(3011, "对象关联的数据库字段", ObjToDataBase),

    SqlToJoinOfMultiDataRel(3012, "对象关联数据库的表(多对一)", SqlToJoin),
    SqlToJoinOfWeakRel(3013, "对象关联数据库的表(非强制关联)", SqlToJoin),
    SqlToJoinOfStrongRel(3014, "对象关联数据库的表(强制关联)", SqlToJoin),

    AttrConstructor(3021, "属性构造定义"),

    DataFuture(3015, "数据特征"),
    Grid(3022, "网格"),
    Col(3017, "列", Grid),
    Head(3016, "头", Grid),
    Cell(3018, "单元", Grid),
    ForeignKey(3019, "外键", SqlToSelect),
    PrimaryKey(3020, "主键", SqlToSelect),

    Sql(3198, "SQL"),
    SqlOperator(3199, "SQL语言关系符", Sql),
    Equal(3200, "等于", SqlOperator),
    NotEqual(3201, "不等于", SqlOperator),
    GreatThan(3202, "大于", SqlOperator),
    LessThan(3203, "小于", SqlOperator),
    OR(3204, "或", SqlOperator),
    AND(3205, "与", SqlOperator),
    Contain(3206, "包含", SqlOperator),
    NotContain(3207, "包含", SqlOperator),
    Exists(3208, "存在", SqlOperator),
    NotExists(3209, "不存在", SqlOperator),
    ItemOfParent(3210, "由上级LINK决定处理方式"),
    Value(3211, "sql最小项", SqlOperator),
    Values(3212, "sql最小项集", SqlOperator),
    GreatEqual(3213, "大于等于", SqlOperator),
    LessEqual(3214, "小于等于", SqlOperator),
    ;

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
    @Deprecated
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


    public boolean isInstanceOf(final LinkMetaType value) {
        if (value == null) {
            return false;
        }
        if (this.equals(value)) {
            return true;
        }
        if (this.getParent() != null) {
            return this.getParent().isInstanceOf(value);
        }
        return false;
    }

    public boolean isAssignableFrom(final LinkMetaType value) {
        if (value == null) {
            return false;
        }
        if (this.equals(value)) {
            return true;
        }
        if (value.getParent() != null) {
            return this.isAssignableFrom(value.getParent());
        }
        return false;
    }
}
