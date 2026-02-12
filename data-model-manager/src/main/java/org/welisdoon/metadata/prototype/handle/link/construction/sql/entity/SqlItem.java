package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.springframework.util.Assert;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.util.*;

/**
 * @Classname SqlItem
 * @Description TODO
 * @Author Septem
 * @Date 11:23
 */
public class SqlItem extends SqlRelationExpression {
    SqlAlias sqlAlias;
    boolean constValue;

    @Override
    protected void build() {
        this.readonly();
        if (getValueId() != null) {
            constValue = true;
            sqlAlias = new SqlAlias("", getValue().getCode());
            return;
        }
        if (getAttribute() instanceof DataObject.Field) {
            Sql last = format(this.<DataObject.Field>getAttribute());
            sqlAlias = new SqlAlias((getPrefix() + "_" + last.getPrefix()).replace("_T", "_"), last.getAttribute().getCode());
        } else
            sqlAlias = new SqlAlias(getPrefix(), getAttribute().getCode());
    }

    public static Sql format(DataObject.Field field) {
        List<MetaLink> list = field.columnMapper();
        ListIterator<MetaLink> listIterator = list.listIterator();
        MetaLink parent = listIterator.next(), current;
        while (listIterator.hasNext()) {
            current = listIterator.next();
            Assert.isTrue(current instanceof Sql, String.format("属性定义配置异常!%s", parent.getId()));
            parent.getChildren().clear();
            parent.getChildren().add(current);
            current.setParent(parent);
            parent = current;
        }
        ((Sql) list.get(0)).build();
        return (Sql) list.get(list.size() - 1);
    }

    @Override
    protected String format(FormatContent format) {
        return String.format("%s%s%s", sqlAlias.alias, constValue ? "" : ".", sqlAlias.getTarget());
    }

    @Override
    String getPrefix() {
        SqlJoiner sqlJoiner = findParent(SqlJoiner.class);
        return getPrefix(sqlJoiner);
    }

    String getPrefix(SqlJoiner sqlJoiner) {
        if (sqlJoiner == null) {
            return "";
        }
        if (matched(sqlJoiner)) {
            return sqlJoiner.getPrefix();
        } else {
            List<MetaLink> metaLinks;
            if (sqlJoiner.getParent() instanceof SqlJoiner) {
                metaLinks = (List) ((SqlJoiner) sqlJoiner.getParent()).subJoiners;
            } else if (sqlJoiner.getParent() != null) {
                metaLinks = sqlJoiner.getParent().getChildren();
            } else {
                return "";
            }
            for (MetaLink child : metaLinks) {
                if (child instanceof SqlJoiner) {
                    if (matched((SqlJoiner) child)) {
                        return ((SqlJoiner) child).getPrefix();
                    }
                }
            }
        }
        return getPrefix(sqlJoiner.findParent(SqlJoiner.class));
    }

    protected boolean matched(SqlJoiner sqlJoiner) {
        return Objects.equals(sqlJoiner.getObjectId(), this.getObjectId()) && Objects.equals(sqlJoiner.getInstanceId(), getInstanceId());
    }
}
