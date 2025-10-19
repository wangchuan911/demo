package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.util.LinkedList;
import java.util.List;
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

    @Override
    protected String format() {
        if (leaf)
            switch (getType()) {
                case ObjConstructor:
                case SqlToJoinOfStrongRel:
                    return String.format(" join %s %s on %s /*line*/", table.getTarget(), table.getAlias(), condition.stream().map(SqlRelationExpression::format).collect(Collectors.joining(" and ")));
                case SqlToJoinOfWeakRel:
                    return String.format(" left join %s %s on %s /*line*/", table.getTarget(), table.getAlias(), condition.stream().map(SqlRelationExpression::format).collect(Collectors.joining(" and ")));
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        else {
            return subJoiners.stream().map(sqlJoiner -> {
                String s = sqlJoiner.format();
                if (isWeakRelation() && s.startsWith(" join ")) {
                    return " left" + s;
                }
                return s;
            }).collect(Collectors.joining(" "));
        }
    }

    protected String formatFirst() {
        if (leaf)
            switch (getType()) {
                case ObjConstructor:
                case SqlToJoinOfStrongRel:
                    return String.format(" from %s %s %s where %s 1=1", table.getTarget(), table.getAlias(), OTHER, condition.stream().map(sql -> sql.format() + " and ").collect(Collectors.joining("")));
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        else {
            return subJoiners.get(0).formatFirst().replace(OTHER,
                    (subJoiners.size() == 1 ? "" : subJoiners.stream().skip(1).map(SqlJoiner::format).collect(Collectors.joining(" "))) + OTHER);
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
}
