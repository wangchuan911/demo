package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SqlBuilder
 * @Description TODO
 * @Author Septem
 * @Date 21:36
 */
@Deprecated
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

    default String buildWheres(SqlContent content) {
        StringBuilder sql = new StringBuilder();
        sql.append(content.getLinks().stream().map(buildNode -> buildNode.buildWheres(content, this)).filter(StringUtils::isNoneBlank).collect(Collectors.joining(" and ")));
        if (sql.length() == 0) {
            return "";
        }
//        StringBuilder sql = Optional.ofNullable(buildWhere(content)).orElseGet(StringBuilder::new);
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
        return content.getLinks().stream().map(buildNode -> buildNode.buildJoins(content, this)).filter(StringUtils::isNoneBlank).collect(Collectors.joining(" "));
    }

    /*default StringBuilder buildWhere(SqlContent content) {
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
    }*/

    default boolean matchLinkMetaType( LinkMetaType linkMetaType,LinkMetaType... linkMetaTypes) {
        return Arrays.stream(linkMetaTypes).anyMatch(linkMetaType1 -> linkMetaType.isMatched(linkMetaType1, Side.Up));
    }

    default String buildWhere(SqlContent sqlContent, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfMultiDataRel:
                MetaLink metaLink = list.get(0);
                String joins = list
                        .stream()
                        .skip(1)
                        .map(metaLink1 -> String
                                .format(" join %s %s on %s",
                                        metaLink1.getObject().getCode(),
                                        sqlContent.getAlias(metaLink1),
                                        metaLink1.getChildren()
                                                .stream().map(child -> ISqlBuilderHandler.getHandler(child.getType()).toSql(child, sqlContent)).collect(Collectors.joining(" and "))))
                        .collect(Collectors.joining());
                return String.format(" exist (select 1 from %s %s %s where %s )", metaLink.getObject().getCode(), sqlContent.getAlias(metaLink), joins, metaLink.getChildren().stream().map(child -> ISqlBuilderHandler.getHandler(child.getType()).toSql(child, sqlContent)).collect(Collectors.joining(" and ")));
        }
        return null;
    }

    default String buildColumns(SqlContent content) {
        String sql = content.getLinks().stream().map(buildNode -> buildNode.buildColumns(content, this)).filter(StringUtils::isNoneBlank).collect(Collectors.joining(","));
        return StringUtils.isAllBlank(sql) ? "*" : sql;
    }
}
