package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoon.common.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "scroll", parentTagTypes = Iterable.class, desc = "遍历器，遍历查询结果，文件等")
public class Scroll extends Unit implements Executable {
    String itemName = "item", itemNames = "items", itemIndex = "index";
    static Map<TaskRequest, Map<Unit, AtomicLong>> indexMap = new HashMap<>();

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
            map.putAll((Map) data.getLastUnitResult());
            super.start(data, toNext);
        } finally {
            map.clear();
        }
    }

    protected Future<Object> scroll(Iterable iterable, TaskRequest data, List<Map> list) {
        try {
            AtomicLong index = ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(indexMap, data, HashMap::new), parent, AtomicLong::new);
            Future<Object> listFuture = Future.succeededFuture();
            switch (MapUtils.getString(attributes, "mode", "item")) {
                case "items":
                    listFuture = listFuture.compose(o -> iterable.iterator(data, Map.of(itemIndex, index.addAndGet(list.size()), itemNames, list)));
                    break;
                default:
                    for (Map row : list) {
                        listFuture = listFuture.compose(o ->
                                iterable.iterator(data, Map.of(itemIndex, index.incrementAndGet(), itemName, row))
                        );
                    }
                    break;
            }
            return listFuture;
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public static Future<Object> iterator(Unit unit, TaskRequest data, Object item) {
        return unit.startChildUnit(data, item, Scroll.class);
    }
}
