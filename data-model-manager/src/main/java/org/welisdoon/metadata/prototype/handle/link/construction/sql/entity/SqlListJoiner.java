package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;

import java.util.stream.Collectors;

/**
 * @Classname SqlListJoiner
 * @Description TODO
 * @Author Septem
 * @Date 20:21
 */
public class SqlListJoiner extends SqlJoiner {
    @Override
    protected String format(IFormatContent format) {
        if (leaf)
            switch (getType()) {
                case SqlToJoinOfMultiDataRel:
                    return String.format("/*multi*/ left join %s %s on %s /*multi*/", table.getTarget(), table.getAlias(), condition.stream().map(sqlRelationExpression -> sqlRelationExpression.format(format)).collect(Collectors.joining(" and ")));
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }

        return super.format(format);
    }

    @Override
    protected boolean isWeakRelation() {
        return super.isWeakRelation() || getType() == LinkMetaType.SqlToJoinOfMultiDataRel;
    }
}
