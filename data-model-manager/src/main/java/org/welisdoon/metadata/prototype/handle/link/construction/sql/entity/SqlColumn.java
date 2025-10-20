package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

/**
 * @Classname SqlColumn
 * @Description TODO
 * @Author Septem
 * @Date 11:36
 */
public class SqlColumn extends Sql{

    @Override
    protected void build() {
        this.readonly();
    }

    @Override
    protected String format(Format format) {
        return null;
    }
}
