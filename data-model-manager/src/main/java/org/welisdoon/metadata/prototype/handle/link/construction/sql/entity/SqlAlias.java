package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

/**
 * @Classname SqlAlias
 * @Description TODO
 * @Author Septem
 * @Date 11:55
 */
class SqlAlias {
    final String alias;
    final String target;

    public SqlAlias(String alias, String target) {
        this.alias = alias;
        this.target = target;
    }

    public String getAlias() {
        return alias;
    }

    public String getTarget() {
        return target;
    }
}
