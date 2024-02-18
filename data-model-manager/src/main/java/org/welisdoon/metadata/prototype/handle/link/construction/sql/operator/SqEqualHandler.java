package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.context.annotation.Primary;
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
@Primary
@LinkMetaType.LinkHandle({LinkMetaType.Equal, LinkMetaType.NotEqual})
public class SqEqualHandler implements ISqlBuilderHandler {

    @Override
    public String toSql(MetaLink parentML, SqlContent parentSB) {
        return linkToSql(parentML) + operator(parentML.getType()) + linkToSql(parentML.getChildren().stream().findFirst().orElse(null));
    }

    @Override
    public String operator(LinkMetaType type) {
        switch (type) {
            case NotEqual:
                return "!=";
            default:
                return "=";
        }
    }
}
