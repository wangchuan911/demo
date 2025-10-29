package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 11:17
 */
public class SqlJoiner extends Sql {
    SqlAlias table;
    List<SqlRelationExpression> condition;
    List<SqlJoiner> subJoiners = new LinkedList<>();
    boolean leaf;
    static final String OTHER = "{other}";

    protected void build() {
        this.readonly();
        MetaObject object = getObject();
        if (object instanceof DataBaseTable) {
            leaf = true;
            table = new SqlAlias(getPrefix(), object.getCode());
            condition = new LinkedList<>();
            for (MetaLink child : getChildren()) {
                if (child instanceof SqlRelationExpression) {
                    ((SqlRelationExpression) child).build();
                    condition.add((SqlRelationExpression) child);
                }
            }
        } else if (object instanceof DataObject) {
            boolean first = true;
            for (MetaLink constructorLink : ((DataObject) object).getConstructorLinks()) {
                if (constructorLink instanceof SqlJoiner) {
                    constructorLink.setParent(this);
                    ((SqlJoiner) constructorLink).build();
                    subJoiners.add((SqlJoiner) constructorLink);
                    if (first) {
                        first = false;
                        for (MetaLink child : getChildren()) {
                            if (child instanceof SqlRelationExpression) {
                                child.setParent(constructorLink);
                                ((SqlRelationExpression) child).build();
                                ((SqlJoiner) constructorLink).condition.add((SqlRelationExpression) child);

                            }
                        }
                    }
                }
            }
        }
    }

//    @Override
//    protected String format(Format format) {
//        if (leaf) {
//            switch (getType()) {
//                case ObjConstructor:
//                case SqlToJoinOfWeakRel:
//                case SqlToJoinOfStrongRel:
//                    return String.format(" %s join %s %s on %s ",
//                            isWeakRelation() ? "left" : "",
//                            table.getTarget(),
//                            table.getAlias(),
//                            Stream.<Stream<String>>of(
//                                    condition.stream().map(sqlRelationExpression -> sqlRelationExpression.format(format)),
//                                    Format.Template == format ? findInputs().stream().map(field -> {
//                                        Sql last = SqlItem.format(field);
//                                        return !Objects.equals(last.getPrefix(), table.getAlias()) ? "" : MessageFormat.format("{0}.{1} = #'{'{2},jdbcType=VARCHAR'}'", last.getPrefix(), last.getAttribute().getCode(), field.getCode());
//                                    }) : Stream.<String>of()
//                            ).flatMap(stringStream -> stringStream).filter(StringUtils::isNoneBlank).collect(Collectors.joining(" and ")));
//                default:
//                    throw new IllegalStateException("不支持的操作：" + getType().name());
//            }
//        } else {
//            MetaObject object = getObject();
//            return String.format("/*%s*/ %s /*%s*/",
//                    object.getName(),
//                    subJoiners.stream().map(sqlJoiner -> {
//                        String s = sqlJoiner.format(format);
//                        if (isWeakRelation() && s.startsWith(" join ")) {
//                            return " left" + s;
//                        }
//                        return s;
//                    }).collect(Collectors.joining(" ")),
//                    object.getName());
//        }
//    }
//
//    protected String formatFirst(Format format) {
//        if (leaf)
//            switch (getType()) {
//                case ObjConstructor:
//                case SqlToJoinOfStrongRel:
//                    return String.format(" from %s %s %s where %s",
//                            table.getTarget(),
//                            table.getAlias(),
//                            OTHER,
//                            Optional.of(
//                                    Stream.<Stream<String>>of(
//                                            condition.stream().map(sql -> sql.format(format)),
//                                            Format.Template == format ? findInputs().stream().map(field -> {
//                                                Sql last = SqlItem.format(field);
//                                                return !Objects.equals(last.getPrefix(), table.getAlias()) ? "" : MessageFormat.format("{0}.{1} = #'{'{2},jdbcType=VARCHAR'}'", last.getPrefix(), last.getAttribute().getCode(), field.getCode());
//                                            }) : Stream.<String>of()
//                                    ).flatMap(stringStream -> stringStream).filter(StringUtils::isNotEmpty).collect(Collectors.joining(" and "))
//                            ).filter(StringUtils::isNoneBlank).orElse("1=1")
//                    );
//                default:
//                    throw new IllegalStateException("不支持的操作：" + getType().name());
//            }
//        else {
//            return subJoiners.get(0).formatFirst(format).replace(OTHER,
//                    (subJoiners.size() == 1 ? "" : subJoiners.stream().skip(1).map(sqlJoiner -> sqlJoiner.format(format)).collect(Collectors.joining(" "))) + OTHER);
//        }
//    }

    @Override
    protected String format(FormatContent format) {
        return format(format, LinkMetaType.ObjConstructor);
    }

    protected String format(FormatContent format, LinkMetaType parentType) {
        boolean isFirst = format.getTableCount() == 0;
        if (leaf) {
            switch (getType()) {
                case SqlToJoinOfWeakRel:
                    Assert.isTrue(!isFirst, "不支持的操作：" + getType().name());
                case ObjConstructor:
                case SqlToJoinOfStrongRel:
                    List<String> cond = condition.stream().map(sql -> sql.format(format)).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                    format.addTablePart(new FormatContent.Part(format, table, cond).setParentType(parentType).setType(getType()));

                    Object[] args = new String[4];
                    args[0] = table.getTarget();
                    args[1] = table.getAlias();
                    args[2] = cond.stream().collect(Collectors.joining(" and "));
                    String template;
                    if (isFirst) {
                        template = " from {0} {1} {3} where {2}";
                        args[3] = OTHER;
                        args[2] = StringUtils.isNoneBlank((String) args[2]) ? args[2] : "1=1";
                    } else {
                        template = " {3} join {0} {1} on {2} ";
                        args[3] = isWeakRelation(parentType) || isWeakRelation(getType()) ? "left" : "";
                    }
                    return MessageFormat.format(template, args);
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        } else {
            try {
                format.deep();
                if (isFirst)
                    return subJoiners.get(0).format(format).replace(OTHER,
                            (subJoiners.size() == 1 ? "" : subJoiners.stream().skip(1).map(sqlJoiner -> sqlJoiner.format(format)).collect(Collectors.joining(" "))) + OTHER);
                else {
                    MetaObject object = getObject();
                    return String.format("/*%s*/ %s /*%s*/",
                            object.getName(),
                            subJoiners.stream().map(sqlJoiner -> sqlJoiner.format(format, getType())).collect(Collectors.joining(" ")),
                            object.getName());
                }
            } finally {
                format.shallow();
            }
        }
    }


    protected boolean isWeakRelation(LinkMetaType type) {
        return type == LinkMetaType.SqlToJoinOfWeakRel;
    }

    @Override
    String getPrefix() {
        SqlJoiner metaLink1 = findParent(SqlJoiner.class);
        if (metaLink1 == null) {
            return "T" + getInstanceIdAsLongValue();
        }
        return metaLink1.getPrefix() + "_" + getInstanceIdAsLongValue();
    }


    protected List<DataObject.Field> findInputs() {
        if (getParent() instanceof SqlJoiner) {
            return ((SqlJoiner) getParent()).findInputs();
        } else if (getParent() instanceof SqlContent) {
            return ((SqlContent) getParent()).findInputs();
        }
        return List.of();
    }


}
