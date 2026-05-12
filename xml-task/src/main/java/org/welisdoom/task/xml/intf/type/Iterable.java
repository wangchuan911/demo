package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.Break;
import org.welisdoom.task.xml.entity.Iterator;
import org.welisdoom.task.xml.entity.TaskSession;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @Classname Iteratable
 * @Description TODO
 * @Author Septem
 * @Date 15:29
 */
public interface Iterable<T> {

    default Future<Object> iterator(TaskSession data, Item<T> item) {
        return Iterator.iterator((Unit) this, data, item);
    }

    default void iteratorSync(TaskSession data, Item<T> item) throws Throwable {
        Iterator.iteratorSync((Unit) this, data, item);
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

    @Deprecated
    default Future<Object> futureLoop(Item<T> item, Future<Object> preFuture, TaskSession data) {
        /*return preFuture.compose(o -> this.iterator(data, Item.of(index.incrementAndGet(), t)));*/
        return preFuture.compose(o -> this.iterator(data, item));
    }

    @Deprecated
    default void waitAMonuments(AtomicLong index, AtomicLong complete) {
        waitAMonuments(index.get(), complete.get());
    }

    @Deprecated
    default void waitAMonuments(long index, long complete) {
        try {
            if (index - complete > 100) {
                wait(1000);
                while (index - complete > 50) {
                    wait(1000);
                }
            }
            if (index % 100 == 0) {
                Thread.sleep(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    default void waitAMonuments(BigDecimal index, BigDecimal complete) {
        waitAMonuments(Math.abs(index.longValue()), Math.abs(complete.longValue()));
    }

    @Deprecated
    default Future<Object> loopEnd(TaskSession data) {
        Optional<Unit> iterator = ((BaseUnit) this).getChild(BaseUnit.typeMatched(Iterator.class)).stream().findFirst();
        if (iterator.isPresent())
            return ((Iterator) iterator.get()).iterateFinish(data);
        return Future.succeededFuture();
    }

    default void await(TaskSession data) throws InterruptedException {
        List<Iterator> list = ((BaseUnit) this).getChild(BaseUnit.typeMatched(Iterator.class));
        for (Iterator o : list) {
            o.await(data);
        }
    }


    /*static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop) {
        return compose(preFuture, loop, Future::failedFuture);
    }

    static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop, Function<Throwable, Future<T>> failureMapper) {
        Promise<K> promise = Promise.promise();
        preFuture.onComplete(promise);
        return preFuture.compose(loop, failureMapper);
    }*/

    @Deprecated
    default Future<Object> bigFutureLoop(Item<T> item, long triggerCount, Future<?> preFuture, TaskSession data) {
        return bigFutureLoop(item.index, triggerCount, preFuture,
                o -> this.iterator(data, item));

    }

    @Deprecated
    static <T, K> Future<T> bigFutureLoop(long count, long triggerCount, Future<K> preFuture, Function<K, Future<T>> loop) {
        if (count % triggerCount == 0) {
            GCUtils.toSafePoint();
            /*Promise<K> promise = Promise.promise();
            preFuture.onComplete(compose(promise));
            return promise.future().compose(loop);*/
//            return compose(preFuture, loop);
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

    /*static long countReset(AtomicLong aLong, long triggerCount, long reset) {
        if (aLong.incrementAndGet() > triggerCount) {
            aLong.set(reset);
        }
        return aLong.get();
    }*/

    public static <T, E extends Throwable> void loop(ObjectUtils.IfNull<T, E> supplier, Looper<T, E> resultConsumer) throws E {
        T t;
        while (Objects.nonNull(t = supplier.get())) {
            try {
                resultConsumer.step(t);
            } catch (Break.SkipOneLoopThrowable e) {
                Break.onContinue(e);
            } catch (Break.BreakLoopThrowable e) {
                Break.onBreak(e);
                break;
            }
        }
    }

    @FunctionalInterface
    interface Looper<T, E1 extends Throwable> {
        void step(T t) throws E1;
    }
}
