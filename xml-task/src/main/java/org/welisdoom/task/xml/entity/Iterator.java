package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.GCUtils;

import java.util.Map;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "iterator", parentTagTypes = Iterable.class, desc = "遍历器，遍历查询结果，文件等")
public class Iterator extends Unit implements Executable {
    String itemName = "item", itemIndex = "index";

    /*protected void execute(TaskRequest data, Map<String, Object> item) throws Throwable {
        Map map = data.getBus(parent.id);
        try {
            map.put(itemName, item);
            execute(data);

    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        Map map = data.getBus(parent.id);
        Iterable.Item item = data.getLastUnitResult();
        map.put(itemIndex, item.getIndex());
        map.put(itemName, item.getItem());
        item.destroy();
        item = GCUtils.release(item);
        Promise<Object> promise = Promise.promise();
        promise.future()
                .onComplete(objectAsyncResult -> {
                    map.remove(itemName);
                    map.remove(itemIndex);
                    if (objectAsyncResult.failed()) {
                        toNext.fail(objectAsyncResult.cause());
                    } else {
                        toNext.complete(objectAsyncResult.result());
                    }
                });
        super.start(data, promise);
    }


    public static Future<Object> iterator(Unit unit, TaskRequest data, Object item) {
        return unit.startChildUnit(data, item, typeMatched(Iterator.class));
    }
}
