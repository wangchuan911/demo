package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.SqlHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTag = "transactional")
public class Select extends Unit implements SqlHandler {

    public void execute(Map<String, Object> data) {
        String sql = getChild(Sql.class).toSql();
        ForEach forEach = getChild(ForEach.class);
        List<Map<String, Object>> list = new LinkedList<>();
        for (Map<String, Object> item : list) {
            data.put("item", item);
            forEach.onData(data);
        }
    }
}
