package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.Objects;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 11:12
 */
public abstract class Sql extends MetaLink {

    String getPrefix() {
        MetaLink metaLink = getParent();
        if (metaLink instanceof Sql)
            return ((Sql) metaLink).getPrefix();
        return "";
    }

    protected void readonly() {
        this.setState(LifeState.Readonly);
    }

    abstract protected void build();

    abstract protected String format();
}
