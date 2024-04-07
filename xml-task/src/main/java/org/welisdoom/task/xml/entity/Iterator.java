package org.welisdoom.task.xml.entity;


import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.LogUtils;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        return thread(data, (Iterable.Item) preUnitResult);
    }

    protected Future<Object> execute(TaskInstance data, Iterable.Item item) {
        Map map = data.getBus(parent.id);
        log(LogUtils.styleString("", 42, 3, String.format("<%s:%s>==>循环第%d次", parent.getClass().getSimpleName(), parent.getId(), item.getIndex())));
        map.put(itemIndex, item.getIndex());
        map.put(itemName, item.getItem());
        item.destroy();
        item = GCUtils.release(item);
        return super.start(data, null)
                .transform(objectAsyncResult ->
                        (objectAsyncResult.succeeded() || (objectAsyncResult.cause() instanceof Break.SkipOneLoopThrowable)) ? Future.succeededFuture() : Future.failedFuture(objectAsyncResult.cause())
                )
                .onComplete(event -> {
                    synchronized (map) {
                        map.remove(itemName);
                        map.remove(itemIndex);
                    }
                });
    }

    public static class ThreadInfo {
        Queue<TaskInstance> idles = new LinkedList<>();
        List<Future> futures = new LinkedList<>();
        final int count;

        ThreadInfo(TaskInstance taskInstance, int threadCount) {
            count = threadCount;
            for (int i = 0; i < threadCount; i++) {
                idles.add(taskInstance.copy("thread-" + (i + 1)));
            }
        }

        synchronized Future<Object> run(Function<TaskInstance, Future<Object>> function) {

            TaskInstance taskInstance = idles.poll();
            futures.add(function.apply(taskInstance).transform(event -> {
                idles.add(taskInstance);
                return event.succeeded() ? Future.succeededFuture(event.result()) : Future.failedFuture(event.cause());
            }));
            if (futures.size() >= count)
                return (Future) CompositeFuture.any(futures).onComplete(event -> {
                    futures.removeAll(futures.stream().filter(Future::isComplete).collect(Collectors.toList()));
                });
            else
                return Future.succeededFuture();
        }

        Future<Object> flush() {
            Future future = CompositeFuture.all(futures);
            futures.clear();
            return future;
        }
    }

    @Override
    protected synchronized void printTag(boolean highLight) {
        super.printTag(highLight);
        String str = String.format("@{{%s}}", Thread.currentThread().getName());
        System.out.print(highLight ? LogUtils.styleString("", ((hashCode() + 1) % 5) + 31, 1, str) : str);
    }

    protected Future<Object> thread(TaskInstance data, Iterable.Item o) {
        switch (threadCount()) {
            case 1:
                return execute(data, o);
            default:
                try {
                    ThreadInfo threadInfo = data.cache(this, () -> new ThreadInfo(data, threadCount()));
                    log("并发-线程中");
                    return threadInfo.run(taskRequest ->
                            Task.getVertx().executeBlocking(event -> {
                                execute(taskRequest, o).onComplete(event1 -> {
                                    if (event1.succeeded()) {
                                        event.complete();
                                    } else {
                                        event.fail(event1.cause());
                                    }
                                });
                            })
                    );
                } catch (Throwable throwable) {
                    return Future.failedFuture(throwable);
                }
        }
    }

    int thread = 1;

    protected int threadCount() {
        return thread;
    }

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        this.thread = Math.min(Math.max(1, MapUtils.getInteger(this.attributes, "thread", this.thread)), CpuCoreSensor.availableProcessors());
        return this;
    }

    public static Future<Object> iterator(Unit unit, TaskInstance data, Object item) {
        return unit.startChildUnit(data, item, typeMatched(Iterator.class));
    }

    @Override
    protected Future<Void> destroy(TaskInstance taskInstance) {
        return super.destroy(taskInstance).onComplete(event -> taskInstance.clearCache(this));
    }

    public Future<Object> iterateFinish(TaskInstance data) {
        ThreadInfo threadInfo = data.cache(this);
        if (threadInfo == null) {
            return Future.succeededFuture();
        }
        return threadInfo.flush();
    }
}
