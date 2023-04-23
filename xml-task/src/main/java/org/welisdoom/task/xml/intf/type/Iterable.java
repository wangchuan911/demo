package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.Scroll;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.entity.Unit;

/**
 * @Classname Iteratable
 * @Description TODO
 * @Author Septem
 * @Date 15:29
 */
public interface Iterable<T> extends UnitType {

    default Future<Object> iterator(TaskRequest data, T item) {
       return Scroll.iterator((Unit) this, data, item);
    }
}
