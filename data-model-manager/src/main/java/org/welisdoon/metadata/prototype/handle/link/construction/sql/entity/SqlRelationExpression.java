package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.common.object.wrapper.IDataAccessObject;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.List;

/**
 * @Classname SqlJoiner
 * @Description TODO
 * @Author Septem
 * @Date 10:36
 */
public class SqlRelationExpression extends Sql {
    SqlRelationExpression left;
    SqlRelationExpression right;

    @Override
    protected void build() {
        this.readonly();
        for (MetaLink child : getChildren()) {
            if (child instanceof SqlRelationExpression) {
                if (left == null) {
                    left = (SqlRelationExpression) child;
                } else if (right == null) {
                    right = (SqlRelationExpression) child;
                } else {
                    throw new IllegalStateException(String.format("表达式解析异常，linkId:%s", getId()));
                }
                ((SqlRelationExpression) child).build();
            }
        }
    }

    protected boolean isColumn(SqlRelationExpression item) {
        return item instanceof SqlItem && !((SqlItem) item).constValue;
    }

    protected void addCache(FormatContent format, SqlItem a, SqlItem b) {
        List<Object> cache = format.get(this.findParent(SqlJoiner.class));
        cache.add(new IDataAccessObject.Model.TableVO.ColumnVO(a.format(format), a.getName(), b.format(format), IDataAccessObject.ColumnType.Simple, String.class));
    }

    @Override
    protected String format(FormatContent format) {
        boolean isCondition = getType() == LinkMetaType.Equal && isColumn(left) && isColumn(right);
        String val = toSql(format);
        if (!isCondition) {
            List<Object> cache = format.get(this.findParent(SqlJoiner.class));
            cache.add(val);
        } else {
            SqlJoiner sqlJoiner = left.findParent(SqlJoiner.class);
            if (sqlJoiner.table.getAlias().equalsIgnoreCase(((SqlItem) left).sqlAlias.alias)) {
                addCache(format, (SqlItem) left, (SqlItem) right);
            } else if (sqlJoiner.table.getAlias().equalsIgnoreCase(((SqlItem) right).sqlAlias.alias)) {
                addCache(format, (SqlItem) right, (SqlItem) left);
            } else {
                throw new IllegalStateException(String.format("不允许将sql语句:%s 放在其他表之内", val));
            }
        }
        return val;
    }

    protected String toSql(FormatContent format) {
//        Assert.isTrue(getType().getParent() == LinkMetaType.SqlOperator, "错误的操作符:" + getType().name());
        switch (getType()) {
            case Equal:
                return String.format(" %s = %s ", left.format(format), right.format(format));
            case NotEqual:
                return String.format(" %s != %s ", left.format(format), right.format(format));
            case GreatThan:
                return String.format(" %s > %s ", left.format(format), right.format(format));
            case LessThan:
                return String.format(" %s < %s ", left.format(format), right.format(format));
            case GreatEqual:
                return String.format(" %s >= %s ", left.format(format), right.format(format));
            case LessEqual:
                return String.format(" %s <= %s ", left.format(format), right.format(format));
            case Contain:
                return String.format(" %s in %s ", left.format(format), right.format(format));
            case NotContain:
                return String.format(" %s not in %s ", left.format(format), right.format(format));
            case OR:
                return String.format("( %s or %s )", left.format(format), right.format(format));
            case AND:
                return String.format("( %s and %s )", left.format(format), right.format(format));
            case Exists:
                return String.format(" exists ( %s and %s)", left.format(format), right.format(format));
            case NotExists:
                return String.format(" not exists ( %s and %s)", left.format(format), right.format(format));
//            case Value:
//            case Values:
            default:
                throw new IllegalStateException("错误的操作符:" + getType().name());
        }
    }
}
