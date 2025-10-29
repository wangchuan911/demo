package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SqlListJoiner
 * @Description TODO
 * @Author Septem
 * @Date 20:21
 */
public class SqlListJoiner extends SqlJoiner {
    @Override
    protected String format(FormatContent format, LinkMetaType parentType) {
        if (leaf)
            switch (getType()) {
                case SqlToJoinOfMultiDataRel:
                    List<String> cond = condition.stream().map(sqlRelationExpression -> sqlRelationExpression.format(format)).collect(Collectors.toList());

                    format.addTablePart(new FormatContent.Part(format, table, cond).setParentType(parentType).setType(getType()));

                    Object[] args = new String[3];
                    args[0] = table.getTarget();
                    args[1] = table.getAlias();
                    args[2] = cond.stream().collect(Collectors.joining(" and "));
                    return MessageFormat.format(" left join {0} {1} on {2} ", args);
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }

        return this.format(format, LinkMetaType.SqlToJoinOfMultiDataRel);
    }

    @Override
    protected boolean isWeakRelation(LinkMetaType type) {
        return super.isWeakRelation(type) || type == LinkMetaType.SqlToJoinOfMultiDataRel;
    }
}
