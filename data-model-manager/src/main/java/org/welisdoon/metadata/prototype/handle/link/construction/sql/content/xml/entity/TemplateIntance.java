package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import org.welisdoon.metadata.prototype.condition.Page;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node.Mappers;

import java.util.List;

/**
 * @Classname TemplatelParameter
 * @Description TODO
 * @Author Septem
 * @Date 9:50
 */
public class TemplateIntance {
    String database;
    String nameSpace;

    List params;
    List input;
    Mappers.Mapper template;
    List column;
    Page page;

    public SqlParameter getSqlParameter() {
        return new SqlParameter();
    }

    public TemplateIntance() {

    }

    public TemplateIntance(MetaObject metaObject) {
        database = "demo";
        nameSpace = metaObject.getCode();
    }

}
