package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.util.Assert;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.metadata.prototype.handle.MetaObjectUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Classname SqlBuilder
 * @Description TODO
 * @Author Septem
 * @Date 19:40
 */
public class SqlBuilder {
    List<SqlJoiner> joins;

    public static class SqlJoiner {
        MetaLink metaLink;
        List<SqlJoinCondition> sqlJoinConditions = new LinkedList<>();

        public SqlJoiner(MetaLink metaLink) {
            this.metaLink = metaLink;
        }

        public SqlJoiner addCondition(SqlJoinCondition sqlJoinCondition) {
            sqlJoinConditions.add(sqlJoinCondition);
            return this;
        }

        public MetaLink getMetaLink() {
            return metaLink;
        }
    }

    public static class SqlJoinCondition extends SqlJoiner {

        public SqlJoinCondition(MetaLink metaLink) {
            super(metaLink);
        }
    }

    public void setJoins(List<SqlJoiner> joins) {
        this.joins = joins;
    }

    public void addJoin(SqlJoiner join) {
        this.joins.add(join);
    }

    static void joinTable(StringBuilder builder, SqlJoiner sqlJoiner) {
        builder.append(MetaObjectUtils.getParent(sqlJoiner.getMetaLink().getObject(), ObjectMetaType.Table).getCode()).append(" ").append("T").append(sqlJoiner.getMetaLink().getInstanceId());
    }

    static void joinCondition(StringBuilder builder, SqlJoiner sqlJoiner) {
        builder.append(MetaObjectUtils.getParent(sqlJoiner.getMetaLink().getObject(), ObjectMetaType.Table).getCode()).append(" ").append("T").append(sqlJoiner.getMetaLink().getInstanceId());
    }

    public String toSql(HandleContext handleContext) {
        Iterator<SqlJoiner> iterable = joins.iterator();
        Assert.isTrue(iterable.hasNext(), "无SQL");
        SqlJoiner sqlJoiner = iterable.next();
        StringBuilder builder = new StringBuilder("select 1 from ");
        joinTable(builder, sqlJoiner);
        List<SqlJoiner> noSingleJoiners = new LinkedList<>();
        while (iterable.hasNext()) {
            sqlJoiner = iterable.next();
            if (LinkMetaType.ObjectLinkSingleLineTable.getId() != sqlJoiner.getMetaLink().getObject().getTypeId()) {
                noSingleJoiners.add(sqlJoiner);
                continue;
            } else {
                builder.append("\n join ");
                joinTable(builder, sqlJoiner);
                builder.append("\n on ");
                joinCondition(builder, sqlJoiner);
            }
        }
        return builder.toString();
    }
}
