package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import org.welisdoom.task.xml.intf.type.Context;
import org.welisdoon.metadata.prototype.condition.Page;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname SqlParameter
 * @Description TODO
 * @Author Septem
 * @Date 9:14
 */
public class SqlParameter extends Context {
    String sql;
    List<Object> params = new LinkedList<>();

    public SqlParameter() {

    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void addParam(Object o) {
        params.add(o);
    }

}
