package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
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

    protected Future<Object> execute(TaskRequest data, Map<String, Object> item) throws Throwable{
        Map map = data.getBus(parent.id);
        try {
            map.put(itemName, item);
            return execute(data);
        } finally {
            map.remove(itemName);
        }
    }
}
