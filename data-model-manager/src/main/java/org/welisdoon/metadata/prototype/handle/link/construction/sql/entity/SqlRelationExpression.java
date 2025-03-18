package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;

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

    @Override
    protected String format() {
//        Assert.isTrue(getType().getParent() == LinkMetaType.SqlOperator, "错误的操作符:" + getType().name());

        switch (getType()) {
            case Equal:
                return String.format(" %s = %s ", left.format(), right.format());
            case NotEqual:
                return String.format(" %s != %s ", left.format(), right.format());
            case GreatThan:
                return String.format(" %s > %s ", left.format(), right.format());
            case LessThan:
                return String.format(" %s < %s ", left.format(), right.format());
            case GreatEqual:
                return String.format(" %s >= %s ", left.format(), right.format());
            case LessEqual:
                return String.format(" %s <= %s ", left.format(), right.format());
            case Contain:
                return String.format(" %s in %s ", left.format(), right.format());
            case NotContain:
                return String.format(" %s not in %s ", left.format(), right.format());
            case OR:
                return String.format("( %s or %s )", left.format(), right.format());
            case AND:
                return String.format("( %s and %s )", left.format(), right.format());
            case Exists:
                return String.format(" exists ( %s and %s)", left.format(), right.format());
            case NotExists:
                return String.format(" not exists ( %s and %s)", left.format(), right.format());
//            case Value:
//            case Values:
            default:
                throw new IllegalStateException("错误的操作符:" + getType().name());
        }
    }
}
