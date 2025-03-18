package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import java.util.stream.Collectors;

/**
 * @Classname SqlListJoiner
 * @Description TODO
 * @Author Septem
 * @Date 20:21
 */
public class SqlListJoiner extends SqlJoiner {
    @Override
    protected String format() {
        if (leaf)
            switch (getType()) {
                case SqlToJoinOfMultiDataRel:
                    return String.format("/*multi*/ left join %s %s on %s /*line*/", table.getTarget(), table.getAlias(), condition.stream().map(SqlRelationExpression::format).collect(Collectors.joining(" and ")));
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }

        return super.format();
    }
}
