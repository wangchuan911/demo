package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

/**
 * @Classname SqlItem
 * @Description TODO
 * @Author Septem
 * @Date 11:23
 */
public class SqlItem extends SqlRelationExpression {
    SqlAlias sqlAlias;
    boolean constValue;

    @Override
    protected void build() {
        this.readonly();
        if (getValueId() != null) {
            constValue = true;
            sqlAlias = new SqlAlias("", getValue().getCode());
            return;
        }
        sqlAlias = new SqlAlias(getPrefix() + "_" + getInstanceIdAsLongValue(), getAttribute().getCode());
    }

    @Override
    protected String format() {
        return String.format("%s%s%s", sqlAlias.alias, constValue ? "" : ".", sqlAlias.getTarget());
    }
}
