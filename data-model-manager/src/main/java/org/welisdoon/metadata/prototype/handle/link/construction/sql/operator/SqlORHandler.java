package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.core.annotation.Order;
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
@LinkMetaType.LinkHandle(LinkMetaType.OR)
@Order(0)
public class SqlORHandler implements ISqlBuilderHandler {

    @Override
    public String toSql(MetaLink metaLink, SqlContent content) {
        return metaLink.getChildren().stream().map(metaLink1 -> toChildSql(metaLink1, content)).collect(Collectors.joining(String.format(" %s ", operator(metaLink.getType()))));
    }
}
