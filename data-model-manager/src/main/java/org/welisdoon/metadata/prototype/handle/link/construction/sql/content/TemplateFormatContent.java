package org.welisdoon.metadata.prototype.handle.link.construction.sql.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.FormatContent;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.Sql;

/**
 * @Classname TemplateBuilder
 * @Description TODO
 * @Author Septem
 * @Date 16:28
 */
public abstract class TemplateFormatContent extends FormatContent {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
//
    public abstract void build();
//
    public abstract Object getValue();
}
