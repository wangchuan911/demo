package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class})
public class Select extends Unit implements Script {

    public void execute(TaskRequest data) {
        data.generateData(this);
        String sql = getScript(data.getBus(), " ");
        System.out.println(sql);
        Iterate iterate = getChild(Iterate.class).get(0);
        List<Map<String, Object>> list = List.of(Map.of("test1", "test1", "test2", "test2", "test3", "test3", "test4", "test4"));
        for (Map<String, Object> item : list) {
            iterate.execute(data, item);
        }
    }

    @Override
    public String getScript(Map<String, Object> data, String s) {
        return getChild(Sql.class).get(0).getScript(data, s).trim();
    }
}
