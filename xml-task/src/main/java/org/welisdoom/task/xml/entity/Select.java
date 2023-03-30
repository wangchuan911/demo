package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.SqlHandler;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class})
public class Select extends Unit implements SqlHandler, Script {

    public void execute(Map<String, Object> data) {
        String sql = getChild(Sql.class).get(0).toSql();
        Iterate iterate = getChild(Iterate.class).get(0);
        List<Map<String, Object>> list = List.of(Map.of("test1", "test1", "test2", "test2", "test3", "test3", "test4", "test4"));
        for (Map<String, Object> item : list) {
            iterate.execute(data, item);
        }
    }
}
