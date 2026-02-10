package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.LogUtils;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    protected String itemName = "item", itemIndex = "index";

    /*protected void execute(TaskRequest data, Map<String, Object> item) throws Throwable {
        Map map = data.getBus(parent.id);
        try {
            map.put(itemName, item);
            execute(data);

    }*/

    @Override
    protected void startSync(TaskSession data) throws Throwable {
        Iterable.Item<?> item = (Iterable.Item<?>) data.getValue();
        switch (threadCount()) {
            case 1:
                executeSync(data, item);
                break;
            default:
                AbstractThreadInfo threadInfo = data.cache(this, () -> new ThreadInfoSync(data, threadCount(), Long.parseLong(attributes.getOrDefault("timeout", "1")), TimeUnit.valueOf(attributes.getOrDefault("time-unit", "MINUTES"))));
                log("并发-线程中");
                threadInfo.run(taskRequest ->
                        {
                            executeSync(taskRequest, item);
                        }
                );
                break;

        }
    }

    @Override
    protected Future<Object> start(TaskSession data, Object preUnitResult) {
        return thread(data, (Iterable.Item) preUnitResult);
    }

    @Deprecated
    protected Future<Object> execute(TaskSession data, Iterable.Item item) {
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

    protected void executeSync(TaskSession data, Iterable.Item<?> item) throws Throwable {
        data.generateData(parent);
        Map map = data.getBus(parent.id);
        {
            log(LogUtils.styleString("", 42, 3, String.format("<%s:%s>==>循环第%d次", parent.getClass().getSimpleName(), parent.getId(), item.getIndex())));
            map.put(itemName, item.getItem());
            map.put(itemIndex, item.getIndex());
            item.destroy();
            data.setValue(GCUtils.release(item));
        }
        try {
            super.startSync(data);
        } finally {
            synchronized (map) {
                map.remove(itemName);
                map.remove(itemIndex);
            }
        }
    }

    @Deprecated
    public static class ThreadInfo {
        Queue<TaskSession> idles = new LinkedList<>();
        List<Future> futures = new LinkedList<>();
        final int count;
        ExecutorService executorService;

        ThreadInfo(TaskSession taskSession, int threadCount) {
            count = threadCount;
            executorService = Executors.newFixedThreadPool(threadCount);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
        }

        synchronized Future<Object> run(Function<TaskSession, Future<Object>> function) {

            TaskSession taskSession = idles.poll();
            futures.removeAll(futures.stream().filter(Future::isComplete).collect(Collectors.toList()));
            futures.add(function.apply(taskSession).onComplete(event -> {
                idles.add(taskSession);
            }));
            if (futures.size() >= count)
                return (Future) Future.any((List) futures);
            else
                return Future.succeededFuture();
        }

        synchronized Future<Object> flush() {
            Future future = Future.all((List) futures);
            futures.clear();
            return future;
        }

    }

    public static abstract class AbstractThreadInfo {
        final int count;
        final AtomicInteger counter = new AtomicInteger();
        final long wait;
        final TimeUnit unit;
        Throwable stop;

        AbstractThreadInfo(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            count = threadCount;
            this.wait = wait;
            this.unit = unit;
        }

        synchronized void setError(Throwable e) {
            boolean pass = (e instanceof Break.SkipOneLoopThrowable || (e instanceof Break.BreakLoopThrowable && ((Break.BreakLoopThrowable) e).decrementAndGetDeep() <= 0));
            if (!pass) {
                stop = e;
            }
        }

        synchronized void isBreak() throws Throwable {
            try {
                if (stop != null) {
                    throw stop;
                }
            } finally {
                this.stop = null;
            }
        }

        abstract void run(ThreadRunner function) throws Throwable;

        abstract void await() throws InterruptedException;
    }

    public static class ThreadInfoSync extends AbstractThreadInfo {
        final LinkedBlockingQueue<TaskSession> idles = new LinkedBlockingQueue<>();

        ThreadInfoSync(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            super(taskSession, threadCount, wait, unit);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
        }

        synchronized void run(ThreadRunner function) throws Throwable {
            isBreak();
            TaskSession taskSession = idles.take();
            if (idles.isEmpty())
                run(function, taskSession);
            else
                new Thread(() -> {
                    counter.incrementAndGet();
                    run(function, taskSession);
                }).run();
        }

        protected void run(ThreadRunner function, TaskSession taskSession) {
            try {
                function.run(taskSession);
            } catch (Throwable e) {
                setError(e);
            } finally {
                try {
                    idles.put(taskSession);
                } catch (Throwable e) {

                }
            }
        }

        synchronized void await() throws InterruptedException {
            for (int i = 0; i < count; i++) {
                idles.poll(wait, unit);
            }
        }

    }

    public static class ThreadInfoSync2 extends AbstractThreadInfo {
        final ExecutorService EXECUTOR_SERVICE;
        final LinkedList<TaskSession> idles = new LinkedList<>();

        ThreadInfoSync2(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            super(taskSession, threadCount, wait, unit);
            EXECUTOR_SERVICE = Executors.newFixedThreadPool(threadCount);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
        }

        void run(ThreadRunner function) throws Throwable {
            isBreak();
            TaskSession taskSession;
            synchronized (idles) {
                taskSession = idles.pollFirst();
            }
            if (idles.isEmpty()) {
                run(function, taskSession);
            } else {
                EXECUTOR_SERVICE.execute(() -> {
                    run(function, taskSession);
                });
            }
            while (idles.isEmpty()) {
                Thread.sleep(100);
            }
        }

        @Override
        void await() throws InterruptedException {
            while (idles.size() < count) {
                Thread.sleep(100);
            }
        }

        protected void run(ThreadRunner function, TaskSession taskSession) {
            try {
                function.run(taskSession);
            } catch (Throwable e) {
                setError(e);
            } finally {
                synchronized (idles) {
                    idles.addLast(taskSession);
                }
            }
        }
    }


    @Override
    protected synchronized void printTag(boolean highLight, LogPosition mode) {
        super.printTag(highLight, mode);
        String str = String.format("@{{%s}}", Thread.currentThread().getName());
        System.out.print(highLight ? LogUtils.styleString("", ((hashCode() + 1) % 5) + 31, 1, str) : str);
    }

    protected Future<Object> thread(TaskSession data, Iterable.Item o) {
        switch (threadCount()) {
            case 1:
                return execute(data, o);
            default:
                try {
                    ThreadInfo threadInfo = data.cache(this, () -> new ThreadInfo(data, threadCount()));
                    log("并发-线程中");
                    return threadInfo.run(taskRequest ->
                            execute(taskRequest, o)
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

    @Deprecated
    public static Future<Object> iterator(Unit unit, TaskSession data, Object item) {
        return unit.startChildUnit(data, item, BaseUnit.typeMatched(Iterator.class));
    }

    public static void iteratorSync(Unit unit, TaskSession data, Object item) throws Throwable {
        data.setValue(item);
        unit.startChildUnitSync(data, BaseUnit.typeMatched(Iterator.class));
    }

    @Override
    @Deprecated
    protected Future<Void> destroy(TaskSession taskSession) {
        return super.destroy(taskSession).onComplete(event -> taskSession.clearCache(this));
    }

    @Override
    protected void destroySync(TaskSession taskSession) {
        super.destroySync(taskSession);
        taskSession.clearCache(this);
    }

    @Deprecated
    public Future<Object> iterateFinish(TaskSession data) {
        ThreadInfo threadInfo = data.cache(this);
        if (threadInfo == null) {
            return Future.succeededFuture();
        }
        return threadInfo.flush();
    }

    public void await(TaskSession data) throws InterruptedException {
        AbstractThreadInfo threadInfo = data.cache(this);
        if (threadInfo == null) {
            return;
        }
        threadInfo.await();
    }

    @FunctionalInterface
    public interface ThreadRunner {
        void run(TaskSession t) throws Throwable;
    }

}
