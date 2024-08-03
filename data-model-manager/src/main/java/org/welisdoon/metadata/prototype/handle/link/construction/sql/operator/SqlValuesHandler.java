package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.stream.Collectors;

/**
 * @Classname SqlJoinHandler
 * @Description TODO
 * @Author Septem
 * @Date 21:48
 */
@Component
@LinkMetaType.LinkHandle({LinkMetaType.Values})
public class SqlValuesHandler implements ISqlBuilderHandler {
    @Override
    public String toSql(MetaLink metaLink, SqlContent content) {
        return String.format("(%s)", metaLink.children().stream().map(metaLink1 -> toChildSql(metaLink1, content)).collect(Collectors.joining(",")));
    }
}
