package org.welisdoom.task.xml.entity;


import java.util.Iterator;
import java.util.Map;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "iterate", parentTag = {Select.class})
public class Iterate extends Unit {
    String itemName = "item";

    @Override
    public void execute(Map<String, Object> data) {
        for (Unit child : children) {
            child.execute(data);
        }
    }

    protected void execute(Map<String, Object> data, Map<String, Object> item) {
        try {
            data.put(itemName, item);
            execute(data);
        } finally {
            data.remove(itemName);
        }
    }
}
