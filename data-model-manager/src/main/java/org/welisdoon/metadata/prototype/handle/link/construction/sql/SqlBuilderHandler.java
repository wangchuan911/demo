package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;

import java.util.function.Function;

/**
 * @Classname ObjectConstructorHandler
 * @Description TODO
 * @Author Septem
 * @Date 23:56
 */
@LinkMetaType.LinkHandle(LinkMetaType.SQLToJoin)
@Component
public class SqlBuilderHandler implements LinkHandle {


    @Override
    public void handler(HandleContext handleContext, MetaLink metaLink) {
        final SqlBuilder sqlBuilder = handleContext.get(this, SqlBuilder::new);
        final SqlBuilder.SqlJoiner sqlJoiner = new SqlBuilder.SqlJoiner(metaLink);
        sqlBuilder.addJoin(sqlJoiner);
        addCondition(handleContext, metaLink, sqlJoiner, (metaLink1) -> new SqlBuilder.SqlJoinCondition(metaLink1));
    }

    protected void addCondition(final HandleContext handleContext, MetaLink metaLink, final SqlBuilder.SqlJoiner sqlJoiner, Function<MetaLink, SqlBuilder.SqlJoinCondition> linkHandle) {
        execute(handleContext, metaLink, (handleContext1, metaLinks1) -> {
            final SqlBuilder.SqlJoinCondition sqlJoinCondition = linkHandle.apply(metaLinks1);
            sqlJoiner.addCondition(sqlJoinCondition);
            addCondition(handleContext1, metaLinks1, sqlJoinCondition, linkHandle);
        });
    }
}
