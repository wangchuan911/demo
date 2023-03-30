package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.type.Script;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "sql", parentTagTypes = Script.class)
public class Sql extends Unit implements Script {
    String toSql() {
        return content;
    }
}
