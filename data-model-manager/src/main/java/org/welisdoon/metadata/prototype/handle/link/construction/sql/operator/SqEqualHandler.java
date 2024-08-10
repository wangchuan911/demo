package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

/**
 * @Classname SqlAndHandler
 * @Description TODO
 * @Author Septem
 * @Date 16:49
 */
@Component
@LinkMetaType.LinkHandle({LinkMetaType.Equal, LinkMetaType.NotEqual, LinkMetaType.GreatThan, LinkMetaType.LessThan})
public class SqEqualHandler implements ISqlBuilderHandler {

    @Override
    public String toSql(MetaLink metaLink, SqlContent content) {
        return toChildSql(metaLink.getChildren().get(0), content) + operator(metaLink.getType()) + toChildSql(metaLink.getChildren().get(1), content);
    }

    @Override
    public String operator(LinkMetaType type) {
        switch (type) {
            case NotEqual:
                return "!=";
            case GreatThan:
                return ">";
            case LessThan:
                return "<";
            case Equal:
                return "=";
            default:
                throw new IllegalArgumentException("error operator!");
        }
    }
}
