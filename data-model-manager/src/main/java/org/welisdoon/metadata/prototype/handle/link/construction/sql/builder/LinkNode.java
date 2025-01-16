package org.welisdoon.metadata.prototype.handle.link.construction.sql.builder;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.ISqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlBuilder;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname TableNode
 * @Description TODO
 * @Author Septem
 * @Date 9:16
 */
public class LinkNode implements BuildNode {
    MetaLink link;

    public LinkNode(MetaLink link) {
        this.link = link;
    }

    @Override
    public long getInstanceId() {
        return link.getInstanceId();
    }

    @Override
    public String buildJoins(SqlContent content, SqlBuilder sqlBuilder) {
        if (!sqlBuilder.matchLinkMetaType(link.getType(), LinkMetaType.SqlToJoin)) {
            return "";
        }
        if (isMain(content, link)) {
            return String.format(" from %s %s", link.getObject().getCode(), content.getAlias(link));
        }
        switch (link.getType()) {
            case SqlToJoinOfStrongRel:
                return buildJoin(content, "join", "on", link);
            case SqlToJoinOfWeakRel:
            case SqlToJoinOfMultiDataRel:
                return buildJoin(content, "left join", "on", link);
        }
        return "";
    }

    @Override
    public String buildWheres(SqlContent content, SqlBuilder sqlBuilder) {
        if (!sqlBuilder.matchLinkMetaType(link.getType(), LinkMetaType.SqlToJoin)) {
            return sqlBuilder.buildWhere(content, link.getType(), List.of(link));
        }
        return null;
    }

    @Override
    public String buildColumns(SqlContent content, SqlBuilder sqlBuilder) {
        Stream<BuildNode> stream = content.getLinks().stream();
        return stream.map(buildNode -> "未完成")
                .collect(Collectors.joining(" and "));
    }

    public boolean isMain(final SqlContent content, MetaLink link) {
        SqlContent content1 = content;
        if (getInstanceId() == 1L) {
            if (content1 instanceof SqlContentNode) {
                content1 = ((SqlContentNode) content1).getUpperContent();
                while (content1 instanceof SqlContentNode && ((SqlContentNode) content1).getInstanceId() == 1L) {
                    content1 = ((SqlContentNode) content1).getUpperContent();
                }
            }
        }
        return link.getInstanceId() == 1L && !(content1 instanceof SqlContentNode);
    }

    protected String buildJoin(SqlContent content, String joinOpr, String condOpr, MetaLink metaLink) {
        return String.format(" %s %s %s %s %s", joinOpr, metaLink.getObject().getCode(), content.getAlias(metaLink), condOpr, metaLink.getChildren().stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, content);
        }).collect(Collectors.joining(" and ")));
    }
}
