package org.welisdoom.task.xml.intf.type;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.entity.Iterator;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoon.common.GCUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

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

    default Future<Object> futureLoop(T t, AtomicLong index, Future<?> preFuture, TaskRequest data) {
        return preFuture.compose(o -> this.iterator(data, Item.of(index.incrementAndGet(), t)));
    }

    default Future<Object> bigFutureLoop(T t, long index, long triggerCount, Future<?> preFuture, TaskRequest data) {
        return bigFutureLoop(index, triggerCount, preFuture,
                o -> this.iterator(data, Item.of(index, t)));

    }

    static <T, K> Future<T> bigFutureLoop(long count, long triggerCount, Future<K> preFuture, Function<K, Future<T>> loop) {
        if (count % triggerCount == 0) {
            GCUtils.toSafePoint();
            Promise<K> promise = Promise.promise();
            preFuture.onComplete(bigFutureLoop(promise));
            return promise.future().compose(loop);
        }
        return preFuture.compose(loop);
    }

    static <K> Handler<AsyncResult<K>> bigFutureLoop(Promise<K> promise) {
        return (kAsyncResult -> {
            if (kAsyncResult.failed())
                promise.fail(kAsyncResult.cause());
            else
                promise.complete(kAsyncResult.result());
        });
    }

    static long countReset(AtomicLong aLong, long triggerCount, long reset) {
        if (aLong.incrementAndGet() > triggerCount) {
            aLong.set(reset);
        }
        return aLong.get();
    }
}
