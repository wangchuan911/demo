package org.welisdoon.metadata.prototype.handle.link.construction.sql;

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
            return String.format("select %s %s %s ", buildColumns(content), buildJoins(content), buildWhere(content));
        } finally {
            content.lock(false);
        }
    }

    String buildColumns(SqlContent content);

    String buildJoins(SqlContent content);

    String buildWhere(SqlContent content);
}
