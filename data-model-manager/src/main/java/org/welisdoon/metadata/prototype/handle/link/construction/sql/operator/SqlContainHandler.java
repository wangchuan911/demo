package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

/**
 * @Classname SqlContainHandler
 * @Description TODO
 * @Author Septem
 * @Date 21:53
 */
@Component
@LinkMetaType.LinkHandle({LinkMetaType.NotContain, LinkMetaType.Contain})
public class SqlContainHandler implements ISqlBuilderHandler {
    @Override
    public String toSql(MetaLink metaLink, SqlContent sqlContent) {
        return linkToSql(metaLink.getChildren().get(0)) + operator(metaLink.getType()) + linkToSql(metaLink.getChildren().get(1));
    }

    @Override
    public String operator(LinkMetaType type) {
        switch (type) {
            case NotContain:
                return " not in ";
            case Contain:
                return " in ";
            default:
                throw new IllegalArgumentException("error operator!");
        }
    }
}
