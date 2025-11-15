package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import org.welisdoom.task.xml.intf.type.Context;

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
    SqlType sqlType;

    public SqlParameter(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void addParam(Object o) {
        params.add(o);
    }

    public enum SqlType {
        DML, DQL, DDL;
    }
}
