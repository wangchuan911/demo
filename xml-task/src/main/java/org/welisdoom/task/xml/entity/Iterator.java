package org.welisdoom.task.xml.entity;


import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
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
@Tag(value = "iterator", parentTagTypes = {Script.class, Stream.class})
public class Iterator extends Unit implements Executable {
    String itemName = "item";

    /*protected void execute(TaskRequest data, Map<String, Object> item) throws Throwable {
        Map map = data.getBus(parent.id);
        try {
            map.put(itemName, item);
            execute(data);

    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        Map map = data.getBus(parent.id);
        try {
            map.put(itemName, data.getLastUnitResult());
            super.start(data, toNext);
        } finally {
            map.remove(itemName);
        }
    }
}
