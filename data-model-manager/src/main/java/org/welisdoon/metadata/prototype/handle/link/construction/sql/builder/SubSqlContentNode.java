package org.welisdoon.metadata.prototype.handle.link.construction.sql.builder;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlBuilder;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.stream.Collectors;

/**
 * @Classname SubSqlContentNode
 * @Description TODO
 * @Author Septem
 * @Date 0:03
 */
@Deprecated
public class SubSqlContentNode extends SqlContentNode {
    MetaLink link;

    public SubSqlContentNode(MetaLink link) {
        this.link = link;
    }

    @Override
    public String buildJoins(SqlContent content, SqlBuilder sqlBuilder) {
        return this.getLinks().stream().map(buildNode -> {
            if (buildNode instanceof LinkNode) {
                switch (((LinkNode) buildNode).link.getType()) {
                    case SqlToJoin:
                        return buildJoin(content, ((LinkNode) buildNode), "join", "on");
                }
            }
            return buildNode.buildJoins(this, sqlBuilder);
        }).filter(StringUtils::isNoneBlank).collect(Collectors.joining(" "));
    }

    protected String buildJoin(SqlContent content, LinkNode linkNode, String joinOpr, String condOpr) {
        return String.format(" %s %s %s %s %s", joinOpr, linkNode.link.getObject().getCode(), this.getAlias(linkNode.link), condOpr, link.getChildren().stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, content);
        }).collect(Collectors.joining(" and ")));
    }
}
