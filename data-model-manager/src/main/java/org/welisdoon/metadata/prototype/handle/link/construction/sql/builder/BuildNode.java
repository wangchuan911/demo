package org.welisdoon.metadata.prototype.handle.link.construction.sql.builder;

import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlBuilder;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;

/**
 * @Classname BuildNode
 * @Description TODO
 * @Author Septem
 * @Date 9:15
 */
public interface BuildNode {

    long getInstanceId();

    String buildJoins(SqlContent sqlContent, SqlBuilder sqlBuilder);

    String buildWheres(SqlContent sqlContent, SqlBuilder sqlBuilder);

    String buildColumns(SqlContent sqlContent, SqlBuilder sqlBuilder);

}
