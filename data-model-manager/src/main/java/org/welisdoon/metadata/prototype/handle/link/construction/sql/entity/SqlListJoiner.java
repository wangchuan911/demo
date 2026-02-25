package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.common.object.wrapper.Prepare;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.text.MessageFormat;
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
    protected String format(FormatContent format) {
        if (leaf) {
            switch (getType()) {
                case SqlToJoinOfMultiDataRel:
                    format.init(this);
                    List<String> cond = condition.stream().map(sqlRelationExpression -> sqlRelationExpression.format(format)).collect(Collectors.toList());
                    /*format.addTablePart(new FormatContent.LeafPart(
                            format,
                            table,
                            cond,
                            findInputs().stream().map(field -> {
                                Sql last = SqlItem.format(field);
                                String alias = last.getPrefix(),
                                        target = last.getAttribute().getCode();
                                return new FormatContent.Part.FormatColumn(alias, target, field);
                            }).collect(Collectors.toList()),
                            getType()));*/

                    Object[] args = new String[3];
                    args[0] = table.getTarget();
                    args[1] = table.getAlias();
                    args[2] = cond.stream().collect(Collectors.joining(" and "));
                    return MessageFormat.format(" left join {0} {1} on {2} ", args);
                default:
                    throw new IllegalStateException("不支持的操作：" + getType().name());
            }
        }

        return this.format(format);
    }

    @Override
    protected boolean isWeakRelation(MetaLink metaLink) {
        return isRelation(metaLink, true, LinkMetaType.SqlToJoinOfMultiDataRel, LinkMetaType.SqlToJoinOfWeakRel);
    }

    @Override
    public int remove() {
        if (getAttribute() != null)
            getAttribute().remove();
        return super.remove();
    }

    protected String getPropertyPrefix() {
        MetaObject.Attribute attribute = this.getAttribute();
        SqlJoiner sqlJoiner = findParent(SqlJoiner.class);
        return MessageFormat.format("{0}{1}{2}", sqlJoiner != null ? sqlJoiner.getPropertyPrefix() : "", attribute.getCode(), Prepare.SPLITTER);
    }
}
