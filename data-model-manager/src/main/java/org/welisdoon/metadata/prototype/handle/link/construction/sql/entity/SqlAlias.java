package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlAlias sqlAlias = (SqlAlias) o;

        if (!Objects.equals(alias, sqlAlias.alias)) return false;
        return Objects.equals(target, sqlAlias.target);
    }

    @Override
    public int hashCode() {
        int result = alias != null ? alias.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }
}
