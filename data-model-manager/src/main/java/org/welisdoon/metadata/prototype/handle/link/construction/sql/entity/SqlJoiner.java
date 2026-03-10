package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.welisdoon.common.object.wrapper.IDataAccessObject;
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
    protected boolean leaf;
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
    protected boolean isFirst() {
        if (leaf) {
            return table.getAlias().matches("T1(\\_1)*");
        } else {
            return subJoiners.get(0).isFirst();
        }
    }


    @Override
    protected String format(FormatContent format) {
        boolean isFirst = isFirst();
        if (leaf) {
            switch (getType()) {
                case SqlToJoinOfWeakRel:
                case ObjConstructor:
                case SqlToJoinOfStrongRel:
                    format.init(this);
                    List<String> cond = condition.stream().map(sql -> sql.format(format)).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                    /*format.addTablePart(new FormatContent.LeafPart(
                            format,
                            table,
                            cond,
                            findInputs().stream().map(field -> {
                                Sql last = SqlItem.format(field);
                                String alias = last.getPrefix(),
                                        target = last.getAttribute().getCode();
                                return new FormatContent.Part.FormatColumn(alias, target, field);
                            }).filter(sql -> Objects.equals(sql.getAlias(), table.getAlias())).collect(Collectors.toList()), getType()));*/

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
                        args[3] = isWeakRelation(this) ? "left" : "";
                    }
                    addCache(format);
                    return MessageFormat.format(template, args);
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        } else {
            /*try {
                format.addTablePart(new FormatContent.VirtualPart(format, getType()));*/
            if (isFirst)
                return subJoiners.get(0).format(format).replace(OTHER,
                        (subJoiners.size() == 1 ? "" : subJoiners.stream().skip(1).map(sqlJoiner -> sqlJoiner.format(format)).collect(Collectors.joining(" "))) + OTHER);
            else {
                MetaObject object = getObject();
                return String.format("/*%s*/ %s /*%s*/",
                        object.getName(),
                        subJoiners.stream().map(sqlJoiner -> sqlJoiner.format(format)).collect(Collectors.joining(" ")),
                        object.getName());
            }

            /*} finally {
                format.pullTablePart();
            }*/
        }
    }

    protected boolean isWeakRelation(MetaLink metaLink) {
        return isRelation(metaLink, true, LinkMetaType.SqlToJoinOfWeakRel);
    }

    final protected boolean isRelation(MetaLink metaLink, boolean findParent, LinkMetaType... types) {
        for (LinkMetaType type : types) {
            if (metaLink.getType() == type) {
                return true;
            }
        }
        while (findParent && (metaLink = metaLink.getParent()) != null) {
            for (LinkMetaType type : types) {
                if (metaLink.getType() == type) {
                    return true;
                }
            }
        }
        return false;
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

    protected String getPropertyPrefix() {
        SqlJoiner sqlJoiner = findParent(SqlJoiner.class);
        if (sqlJoiner != null) {
            return sqlJoiner.getPropertyPrefix();
        }
        return "";
    }

    protected void addCache(FormatContent format) {
        List<Object> cache = format.get(this);
        String value1 = this.getPrefix();
        for (DataObject.Field input : this.findInputs()) {
            Sql last = SqlItem.format(input);
            String value = last.getPrefix();
            if (Objects.equals(value1, value)) {
                cache.add(new IDataAccessObject.Model.TableVO.ColumnVO(
                        MessageFormat.format("{0}.{1}", value, last.getAttribute().getCode()),
                        input.getCode(), null, IDataAccessObject.ColumnType.Simple, String.class));
            }
        }
    }

}
