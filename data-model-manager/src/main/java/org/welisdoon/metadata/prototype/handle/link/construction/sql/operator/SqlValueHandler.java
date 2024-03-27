package org.welisdoon.metadata.prototype.handle.link.construction.sql.operator;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.Objects;

/**
 * @Classname SqlJoinHandler
 * @Description TODO
 * @Author Septem
 * @Date 21:48
 */

@Component
@LinkMetaType.LinkHandle({LinkMetaType.Value})
public class SqlValueHandler implements ISqlBuilderHandler {
    @Override
    public String toSql(MetaLink metaLink, SqlContent sqlContent) {
        if (Objects.nonNull(metaLink.getValueId())) {
            return metaLink.getValue().toSql();
        }
        if (Objects.nonNull(metaLink.getAttributeId()) && Objects.nonNull(metaLink.getInstanceId())) {
            return String.format("T%d.%s", metaLink.getInstanceId(), metaLink.getAttribute().getCode());
        }
        throw new IllegalArgumentException(String.format("不支持当前的link数据转换sql[%s]", metaLink.getId()));
    }
}
