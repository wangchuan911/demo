package org.welisdoon.metadata.prototype.handle.link.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.handle.HandleParameter;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;
import org.welisdoon.metadata.prototype.handle.link.construction.ObjectConstructorHandler;

import java.util.List;

/**
 * @Classname SqlHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:44
 */
@LinkMetaType.LinkHandle({LinkMetaType.ObjectLinkSingleLineTable, LinkMetaType.ObjectLinkMultiLineTable})
public class SqlBuildHandler implements LinkHandle {

    @Autowired
    MetaLinkDao metaObjectDao;

    @Override
    public void handler(HandleParameter handleParameter, MetaLink... metaLinks) {
        ObjectConstructorHandler.ObjectConstructorParams params = handleParameter.getCurrentInstance();
        SqlBuildHandler.SqlBuilder sqlBuilder = new SqlBuildHandler.SqlBuilder();
        handleParameter.setCurrentInstance(sqlBuilder);
        try {
            sqlBuilder.addJoin(new SqlBuildHandler.SqlBuilder.SqlJoiner(metaLinks[0]));
            handler(sqlBuilder, metaObjectDao.list(new MetaLinkCondition().setParentId(metaLinks[0].getId())));
        } finally {
            handleParameter.delCurrentInstance();
        }
    }


    protected void handler(SqlBuildHandler.SqlBuilder sqlBuilder, List<MetaLink> metaLinks) {
        metaLinks
                .stream()
                .filter(metaLink -> {
                    switch (LinkMetaType.getInstance(metaLink.getTypeId())) {
                        case ObjectLinkMultiLineTable:
                        case ObjectLinkSingleLineTable:
                            return true;
                        default:
                            return false;
                    }
                })
                .forEach(metaLink -> {
                    sqlBuilder.addJoin(new SqlBuildHandler.SqlBuilder.SqlJoiner(metaLink));
                });
    }

    public static class SqlBuilder {
        List<SqlJoiner> joins;

        public static class SqlJoiner {
            long instanceId;
            MetaObject metaObject;

            public SqlJoiner(MetaLink metaLink) {
                metaLink.get
            }
        }

        public void setJoins(List<SqlJoiner> joins) {
            this.joins = joins;
        }

        public void addJoin(SqlJoiner join) {
            this.joins.add(join);
        }
    }
}
