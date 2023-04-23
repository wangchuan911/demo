package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.Iterator;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.entity.Unit;

/**
 * @Classname Iteratable
 * @Description TODO
 * @Author Septem
 * @Date 15:29
 */
public interface Iterable<T> extends UnitType {

    default Future<Object> iterator(TaskRequest data, Item<T> item) {
        return Iterator.iterator((Unit) this, data, item);
    }

    class Item<T> {
        long index;
        T item;

        public Item(long index, T item) {
            this.index = index;
            this.item = item;
        }

        public long getIndex() {
            return index;
        }

        public Object getItem() {
            return item;
        }

        public static <T> Item<T> of(long index, T item) {
            return new Item<>(index, item);
        }

        public void destroy() {
            this.item = null;
        }
    }
}
