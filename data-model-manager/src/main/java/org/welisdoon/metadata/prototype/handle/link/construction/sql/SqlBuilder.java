package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname SqlBuilder
 * @Description TODO
 * @Author Septem
 * @Date 21:36
 */
public interface SqlBuilder {

    default String build(SqlContent content) {
        content.isLocked();
        content.lock(true);
        try {
            return String.format("select %s %s %s ", buildColumns(content), buildJoins(content), buildWheres(content));
        } finally {
            content.lock(false);
        }
    }

    default String buildJoin(SqlContent content, String joinOpr, String condOpr, MetaLink metaLink) {
        return String.format(" %s %s %s %s %s", joinOpr, metaLink.getObject().getCode(), content.toTableAlias(metaLink), condOpr, metaLink.getChildren().stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, content);
        }).collect(Collectors.joining(" and ")));
    }


    default String buildWheres(SqlContent content) {
        StringBuilder sql = Optional.ofNullable(buildWhere(content)).orElseGet(StringBuilder::new);
        while (sql.charAt(0) == ' ') {
            sql.delete(0, 1);
        }
        for (int i = sql.length() - 1; sql.charAt(i) == ' '; i--) {
            if (sql.charAt(i) == ' ') {
                sql.delete(i, i + 1);
                continue;
            }
            break;
        }
        if (sql.length() == 0 || sql.lastIndexOf("and") == 0) {
            sql.setLength(0);
            return sql.toString();
        }
        if (sql.lastIndexOf(" and") == sql.length() - 4) {
            sql = sql.delete(sql.length() - 4, 4);
        }
        sql.insert(0, " where ");
        return sql.toString();
    }

    default String buildJoins(SqlContent content) {
        StringBuilder sql = content.getParent() == null ? new StringBuilder() : new StringBuilder(buildJoins(content.getParent()));
        List<LinkMetaType> linkMetaTypes = content.getLinkMetaTypes();
        Stream<MetaLink> stream = content.getLinks().stream();
        if (content.getParent() == null) {
            sql.append(" from ");
            MetaLink mainTable = content.getLinks().stream().findFirst().orElseThrow(() -> new IllegalStateException("缺少主表"));
            sql.append(mainTable.getObject().getCode()).append(" ").append(content.toTableAlias(mainTable));
            stream = stream.skip(1);
        }
        stream.collect(Collectors.groupingBy(MetaLink::getType)).forEach((linkMetaType, list1) -> {
            if (!matchLinkMetaType(linkMetaTypes, linkMetaType)) {
                return;
            }
            buildJoin(content, sql, linkMetaType, list1);
        });
        return sql.toString();
    }

    default void buildJoin(SqlContent content, StringBuilder sql, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfStrongRel:
                list.stream().forEach(metaLink -> {
                    sql.append(buildJoin(content, "join", "on", metaLink));
                });
                break;
            case SqlToJoinOfWeakRel:
                list.stream().forEach(metaLink -> {
                    sql.append(buildJoin(content, "left join", "on", metaLink));
                });
                break;
        }
    }

    default StringBuilder buildWhere(SqlContent content) {
        StringBuilder sql = Optional.ofNullable(content.getParent()).map(this::buildWhere).orElseGet(StringBuilder::new);
        List<LinkMetaType> linkMetaTypes = content.getLinkMetaTypes();
        Stream<MetaLink> stream = content.getLinks().stream();
        if (content.getParent() == null) {
            MetaLink mainTable = content.getLinks().stream().findFirst().orElseThrow(() -> new IllegalStateException("缺少主表"));
            mainTable.getChildren().stream().map(child -> {
                return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, content);
            }).collect(Collectors.joining(" and "));
            stream = stream.skip(1);
            sql.append(" and ");
        }
        stream.collect(Collectors.groupingBy(MetaLink::getType)).forEach((linkMetaType, list1) -> {
            if (!matchLinkMetaType(linkMetaTypes, linkMetaType)) {
                return;
            }
            buildWhere(content, sql, linkMetaType, list1);
        });

        return sql;
    }

    default boolean matchLinkMetaType(List<LinkMetaType> linkMetaTypes, LinkMetaType linkMetaType) {
        return linkMetaTypes.stream().anyMatch(linkMetaType1 -> linkMetaType.isMatched(linkMetaType1, Side.Up));
    }

    default void buildWhere(SqlContent sqlContent, StringBuilder sql, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfMultiDataRel:
                StringBuilder _fromBlock = new StringBuilder();
                StringBuilder _whereBlock = new StringBuilder();
                list.stream().forEach(metaLink -> {
                    StringBuilder _sql, _sql2;
                    String linkTableOpr, LinkCondOpr;
                    if (_fromBlock.length() == 0) {
                        _sql = _fromBlock;
                        _sql2 = _whereBlock;
                        linkTableOpr = " from ";
                        LinkCondOpr = " where ";
                    } else {
                        _sql = _fromBlock;
                        _sql2 = _fromBlock;
                        linkTableOpr = " join ";
                        LinkCondOpr = " on ";
                    }
                    _sql.append(linkTableOpr).append(metaLink.getObject().getCode()).append(sqlContent.toTableAlias(metaLink));
                    _sql2.append(LinkCondOpr);
                    metaLink.getChildren().forEach(child -> {
                        _sql2.append(ISqlBuilderHandler.getHandler(child.getType()).toSql(child, sqlContent));
                    });
                });
                sql.append(" exist (select 1 ").append(_fromBlock).append(_whereBlock).append(")");
                break;
        }
    }

    default String buildColumns(SqlContent content) {
        return "*";
    }
}
