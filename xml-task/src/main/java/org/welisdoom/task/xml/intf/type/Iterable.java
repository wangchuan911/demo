package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.entity.Iterator;
import org.welisdoom.task.xml.entity.TaskInstance;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoon.common.GCUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @Classname Iteratable
 * @Description TODO
 * @Author Septem
 * @Date 15:29
 */
public interface Iterable<T> extends UnitType {

    default Future<Object> iterator(TaskInstance data, Item<T> item) {
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

    default Future<Object> futureLoop(Item<T> item, Future<Object> preFuture, TaskInstance data) {
        /*return preFuture.compose(o -> this.iterator(data, Item.of(index.incrementAndGet(), t)));*/
        return compose(preFuture, o -> this.iterator(data, item));
    }

    default Future<Object> loopEnd(TaskInstance data) {
        Optional<Unit> iterator = ((Unit) this).getChild(Unit.typeMatched(Iterator.class)).stream().findFirst();
        if (iterator.isPresent())
            return ((Iterator) iterator.get()).iterateFinish(data);
        return Future.succeededFuture();
    }


    static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop) {
        return compose(preFuture, loop, Future::failedFuture);
    }

    static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop, Function<Throwable, Future<T>> failureMapper) {
        Promise<K> promise = Promise.promise();
        preFuture.onComplete(event -> {
            if (event.failed())
                promise.fail(event.cause());
            else
                promise.complete(event.result());
        });
        return promise.future().compose(loop, failureMapper);
    }

    default Future<Object> bigFutureLoop(Item<T> item, long triggerCount, Future<?> preFuture, TaskInstance data) {
        return bigFutureLoop(item.index, triggerCount, preFuture,
                o -> this.iterator(data, item));

    }

    static <T, K> Future<T> bigFutureLoop(long count, long triggerCount, Future<K> preFuture, Function<K, Future<T>> loop) {
        if (count % triggerCount == 0) {
            GCUtils.toSafePoint();
            /*Promise<K> promise = Promise.promise();
            preFuture.onComplete(compose(promise));
            return promise.future().compose(loop);*/
            return compose(preFuture, loop);
        }
        return preFuture.compose(loop);
    }

    /*static <K> Handler<AsyncResult<K>> compose(Promise<K> promise) {
        return (kAsyncResult -> {
            if (kAsyncResult.failed())
                promise.fail(kAsyncResult.cause());
            else
                promise.complete(kAsyncResult.result());
        });
    }*/

    static long countReset(AtomicLong aLong, long triggerCount, long reset) {
        if (aLong.incrementAndGet() > triggerCount) {
            aLong.set(reset);
        }
        return aLong.get();
    }
}
