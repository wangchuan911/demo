package org.welisdoon.metadata.prototype.handle.link.construction.sql.builder;

import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlBuilder;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.Objects;

/**
 * @Classname TableNode
 * @Description TODO
 * @Author Septem
 * @Date 9:16
 */
public class SqlContentNode extends SqlContent implements BuildNode {
    SqlContent upperContent;
    long instanceId;

    public SqlContentNode() {
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    public SqlContentNode setInstanceId(long instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public SqlContentNode setUpperContent(SqlContent upperContent) {
        this.upperContent = upperContent;
        return this;
    }

    public SqlContent getUpperContent() {
        return upperContent;
    }

    @Override
    public String buildJoins(SqlContent sqlContent, SqlBuilder sqlBuilder) {
        return sqlBuilder.buildJoins(this);
    }

    @Override
    public String buildWheres(SqlContent sqlContent, SqlBuilder sqlBuilder) {
        return sqlBuilder.buildWheres(this);
    }

    @Override
    public String buildColumns(SqlContent sqlContent, SqlBuilder sqlBuilder) {
        return sqlBuilder.buildColumns(this);
    }

    @Override
    public String getAlias(MetaLink metaLink) {
        StringBuilder alias = new StringBuilder();
        alias.append(metaLink.getInstanceId());

        SqlContent parent = getUpperContent();
        while (parent instanceof SqlContentNode) {
            alias.append("_").append(((SqlContentNode) parent).getInstanceId());
            parent = ((SqlContentNode) parent).getUpperContent();
        }
        return alias.reverse().insert(0, "T").toString();
    }
}
