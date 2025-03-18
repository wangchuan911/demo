package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;

/**
 * @Classname SqlItem
 * @Description TODO
 * @Author Septem
 * @Date 11:23
 */
public class SqlItems extends SqlRelationExpression {

    @Override
    protected void build() {
        this.readonly();
        for (MetaLink child : getChildren()) {
            if (child instanceof Sql) {
                ((Sql) child).readonly();
                ((Sql) child).build();
            }
        }
    }

    @Override
    protected String format() {
        return String.format("(%s)", getChildren().stream().filter(metaLink -> metaLink instanceof Sql).map(metaLink -> ((Sql) metaLink).format()));
    }
}
