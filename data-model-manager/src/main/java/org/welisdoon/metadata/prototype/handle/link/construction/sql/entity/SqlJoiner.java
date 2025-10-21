package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    protected String format(Format format) {
        if (leaf) {
            switch (getType()) {
                case ObjConstructor:
                case SqlToJoinOfWeakRel:
                case SqlToJoinOfStrongRel:
                    return String.format(" %s join %s %s on %s ",
                            isWeakRelation() ? "left" : "",
                            table.getTarget(),
                            table.getAlias(),
                            Stream.<Stream<String>>of(
                                    condition.stream().map(sqlRelationExpression -> sqlRelationExpression.format(format)),
                                    Format.Template == format ? findInputs().stream().map(field -> {
                                        Sql last = SqlItem.format(field);
                                        return !Objects.equals(last.getPrefix(), table.getAlias()) ? "" : MessageFormat.format("{0}.{1} = #'{'{2},jdbcType=VARCHAR'}'", last.getPrefix(), last.getAttribute().getCode(), field.getCode());
                                    }) : Stream.<String>of()
                            ).flatMap(stringStream -> stringStream).filter(StringUtils::isNoneBlank).collect(Collectors.joining(" and ")));
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        } else {
            MetaObject object = getObject();
            return String.format("/*%s*/ %s /*%s*/",
                    object.getName(),
                    subJoiners.stream().map(sqlJoiner -> {
                        String s = sqlJoiner.format(format);
                        if (isWeakRelation() && s.startsWith(" join ")) {
                            return " left" + s;
                        }
                        return s;
                    }).collect(Collectors.joining(" ")),
                    object.getName());
        }
    }

    protected String formatFirst(Format format) {
        if (leaf)
            switch (getType()) {
                case ObjConstructor:
                case SqlToJoinOfStrongRel:
                    return String.format(" from %s %s %s where %s",
                            table.getTarget(),
                            table.getAlias(),
                            OTHER,
                            Optional.of(
                                    Stream.<Stream<String>>of(
                                            condition.stream().map(sql -> sql.format(format)),
                                            Format.Template == format ? findInputs().stream().map(field -> {
                                                Sql last = SqlItem.format(field);
                                                return !Objects.equals(last.getPrefix(), table.getAlias()) ? "" : MessageFormat.format("{0}.{1} = #'{'{2},jdbcType=VARCHAR'}'", last.getPrefix(), last.getAttribute().getCode(), field.getCode());
                                            }) : Stream.<String>of()
                                    ).flatMap(stringStream -> stringStream).filter(StringUtils::isNotEmpty).collect(Collectors.joining(" and "))
                            ).filter(StringUtils::isNoneBlank).orElse("1=1")
                    );
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        else {
            return subJoiners.get(0).formatFirst(format).replace(OTHER,
                    (subJoiners.size() == 1 ? "" : subJoiners.stream().skip(1).map(sqlJoiner -> sqlJoiner.format(format)).collect(Collectors.joining(" "))) + OTHER);
        }
    }


    protected boolean isWeakRelation() {
        return getType() == LinkMetaType.SqlToJoinOfWeakRel;
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
