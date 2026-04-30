package org.welisdoom.task.xml.entity;


import com.alibaba.fastjson.JSON;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
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
                AbstractThreadInfo abstractThreadInfo = data.cache(this, () -> new ThreadInfoSync2(data, threadCount(), Long.parseLong(attributes.getOrDefault("timeout", "1")), TimeUnit.valueOf(attributes.getOrDefault("time-unit", "MINUTES"))));
                log("并发-线程中");
                Thread current = Thread.currentThread();
                abstractThreadInfo.run(taskRequest -> {
                    Thread current2 = Thread.currentThread();
                    if (Objects.equals(current2, current)) {
                        Thread.sleep(100);
                        log("跳过");
                        return;
                    }
                    executeSync(taskRequest, item);
                });
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
//            log((JSON.toJSONString(data.getBus())));
            super.startSync(data);
        } finally {
            synchronized (map) {
                map.clear();
            }
            data.getBus().remove(parent.id);
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
        ExecutorService pool;

        AbstractThreadInfo(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            count = threadCount;
            this.wait = wait;
            this.unit = unit;
        }

        void setError(Throwable e) {
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

        synchronized void run(ThreadRunner function) throws Throwable {
            isBreak();
            TaskSession taskSession = useSession();
            pool.execute(() -> run(function, taskSession));
            counter.incrementAndGet();
        }

        abstract TaskSession useSession() throws Throwable;

        abstract void idleSession(TaskSession taskSession) throws Throwable;

        protected void run(ThreadRunner function, TaskSession taskSession) {
            try {
                function.run(taskSession);
            } catch (Throwable e) {
                setError(e);
            } finally {
                try {
                    idleSession(taskSession);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        abstract void await() throws InterruptedException;

        public void destroy() {
            if (pool.isShutdown()) return;
            pool.shutdown();
        }

    }

    public static class ThreadInfoSync3 extends AbstractThreadInfo {
        final LinkedList<TaskSession> idles = new LinkedList<>();
        Thread thread;

        ThreadInfoSync3(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            super(taskSession, threadCount, wait, unit);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
            pool = Executors.newFixedThreadPool(threadCount);
        }

        @Override
        TaskSession useSession() throws Throwable {
            TaskSession taskSession;
            while ((taskSession = idles.pollFirst()) == null) {
                thread = Thread.currentThread();
                LockSupport.park();
            }
            return taskSession;
        }

        @Override
        void idleSession(TaskSession taskSession) throws Throwable {
            idles.addLast(taskSession);
            if (thread != null) {
                Thread thread2 = thread;
                thread = null;
                LockSupport.unpark(thread2);
            }
        }

        @Override
        void await() throws InterruptedException {
            while (idles.size() >= 0) {
                LockSupport.park();
            }
        }

    }


    @Deprecated
    public static class ThreadInfoSync extends AbstractThreadInfo {
        final LinkedBlockingQueue<TaskSession> idles = new LinkedBlockingQueue<>();
        final static AtomicInteger USING = new AtomicInteger(0);

        ThreadInfoSync(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            super(taskSession, threadCount, wait, unit);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
            USING.incrementAndGet();
            pool = Executors.newCachedThreadPool();
        }

        @Override
        TaskSession useSession() throws Throwable {
            return idles.take();
        }

        @Override
        void idleSession(TaskSession taskSession) throws Throwable {
            idles.put(taskSession);
        }


        synchronized void await() throws InterruptedException {
            for (int i = 0; i < count; i++) {
                idles.poll(wait, unit);
            }
        }


    }

    @Deprecated
    public static class ThreadInfoSync2 extends AbstractThreadInfo {
        final LinkedList<TaskSession> idles = new LinkedList<>();

        ThreadInfoSync2(TaskSession taskSession, int threadCount, Long wait, TimeUnit unit) {
            super(taskSession, threadCount, wait, unit);
            pool = Executors.newFixedThreadPool(threadCount);
            idles.addAll(taskSession.newSession(count, integer -> "thread-" + (integer + 1)));
        }

        void run(ThreadRunner function) throws Throwable {
            super.run(function);
            while (idles.isEmpty()) {
                Thread.sleep(100);
            }
        }

        @Override
        TaskSession useSession() throws Throwable {
            return idles.pollFirst();
        }

        @Override
        void idleSession(TaskSession taskSession) throws Throwable {
            idles.addLast(taskSession);
        }

        @Override
        void await() throws InterruptedException {
            while (idles.size() < count) {
                Thread.sleep(100);
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
        AbstractThreadInfo abstractThreadInfo = taskSession.cache(this);
        if (abstractThreadInfo != null) {
            abstractThreadInfo.destroy();
        }
        super.destroySync(taskSession);
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
        AbstractThreadInfo abstractThreadInfo = data.cache(this);
        if (abstractThreadInfo != null) {
            abstractThreadInfo.await();
        }
    }

    @FunctionalInterface
    public interface ThreadRunner {
        void run(TaskSession t) throws Throwable;
    }

}
