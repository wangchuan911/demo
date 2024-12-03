package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.LinkedList;
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
            case SqlToJoinOfMultiDataRel:
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
        sql.append(stream.
                collect(Collectors.groupingBy(MetaLink::getType))
                .entrySet().stream()
                .filter(entry -> matchLinkMetaType(linkMetaTypes, entry.getKey()))
                .map(entry -> buildWhere(content, entry.getKey(), entry.getValue()))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" and ")));
        return sql;
    }

    default boolean matchLinkMetaType(List<LinkMetaType> linkMetaTypes, LinkMetaType linkMetaType) {
        return linkMetaTypes.stream().anyMatch(linkMetaType1 -> linkMetaType.isMatched(linkMetaType1, Side.Up));
    }

    default String buildWhere(SqlContent sqlContent, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfMultiDataRel:
                List<List<MetaLink>> lists = new LinkedList<>();
                List<MetaLink> links;
                for (MetaLink metaLink : list) {

                }
                return lists.stream().map(list1 -> list1.stream().findFirst().map(metaLink -> {
                    String joins = list1
                            .stream()
                            .skip(1)
                            .map(metaLink1 -> String
                                    .format(" join %s %s on %s",
                                            metaLink1.getObject().getCode(),
                                            sqlContent.toTableAlias(metaLink1),
                                            metaLink1.getChildren()
                                                    .stream().map(child -> ISqlBuilderHandler.getHandler(child.getType()).toSql(child, sqlContent)).collect(Collectors.joining(" and "))))
                            .collect(Collectors.joining());
                    return String.format(" exist (select 1 from %s %s %s where %s )", metaLink.getObject().getCode(), sqlContent.toTableAlias(metaLink), joins, metaLink.getChildren().stream().map(child -> ISqlBuilderHandler.getHandler(child.getType()).toSql(child, sqlContent)).collect(Collectors.joining(" and ")));
                }).orElse("")).collect(Collectors.joining(" and "));
        }
        return null;
    }

    default String buildColumns(SqlContent content) {
        return "*";
    }
}
