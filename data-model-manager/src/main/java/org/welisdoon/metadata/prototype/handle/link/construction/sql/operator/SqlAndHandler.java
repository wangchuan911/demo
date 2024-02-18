package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.stream.Collectors;

/**
 * @Classname SqlAndHandler
 * @Description TODO
 * @Author Septem
 * @Date 16:49
 */
@Component
@LinkMetaType.LinkHandle(LinkMetaType.AND)
public class SqlAndHandler implements ISqlBuilderHandler {

    @Override
    public String toSql(MetaLink parentML, SqlContent parentSB) {
        return parentML.getChildren().stream().map(this::linkToSql).collect(Collectors.joining(" " + operator(parentML.getType()) + " "));
    }
}
