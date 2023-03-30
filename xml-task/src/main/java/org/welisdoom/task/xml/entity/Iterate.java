package org.welisdoom.task.xml.entity;


import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoom.task.xml.intf.type.Stream;

import java.util.Map;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "iterate", parentTagTypes = {Script.class, Stream.class})
public class Iterate extends Unit implements Executable {
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
