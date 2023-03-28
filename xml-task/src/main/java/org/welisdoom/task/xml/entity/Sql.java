package org.welisdoom.task.xml.entity;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "sql", parentTag = "select")
public class Sql extends Unit {
    String toSql() {
        return conntent;
    }
}
