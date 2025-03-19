package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
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
            List<MetaLink> list = ((DataObject.Field) getAttribute()).getColumnMapper();
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
            Sql last = (Sql) list.get(list.size() - 1);
            sqlAlias = new SqlAlias(last.getPrefix(), last.getAttribute().getCode());
        } else
            sqlAlias = new SqlAlias(getPrefix(), getAttribute().getCode());
    }

    @Override
    protected String format() {
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
            for (MetaLink child : sqlJoiner.getParent().getChildren()) {
                if (child == sqlJoiner) {
                    break;
                }
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
