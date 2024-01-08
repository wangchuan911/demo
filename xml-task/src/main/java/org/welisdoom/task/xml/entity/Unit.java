package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.*;
import ognl.Ognl;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.LogUtils;
import org.welisdoon.common.data.IData;
import org.xml.sax.Attributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname Unit
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */
public class Unit implements UnitType, IData<String, Model> {
    String id;
    Unit parent;
    List<Unit> children = new LinkedList<>();
    Map<String, String> attributes = new HashMap<>();

    public Unit setParent(Unit parent) {
        this.parent = parent;
        if (this.parent != null)
            this.parent.children.add(this);
        return this;
    }

    public Unit setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Model getDataMarker() {
        return Model.Unit;
    }

    @Override
    public IData setDataMarker(Model model) {
        return this;
    }

    public String getId() {
        return id;
    }

    public Unit attr(Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(name);
            /*System.out.println("属性值：" + name + "=" + value);*/
            this.attributes.put(name, value);
        }
        this.id = this.attributes.get("id");

        Arrays.stream(this.getClass().getAnnotations()).filter(annotation -> annotation instanceof Attr).map(annotation -> (Attr) annotation).forEach(attr -> {
            if (attr.options().length > 0 && attr.defaultOption() >= 0 && !this.attributes.containsKey(attr.name())) {
                this.attributes.put(attr.name(), attr.options()[attr.defaultOption()]);
            }
        });

        return this;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[" +
                "id:'" + id + '\'' +
                ']';
    }


    public final void destroy() {
        for (Unit child : children) {
            child.destroy();
        }
        this.parent = null;
        this.children.clear();
        this.children = null;
        this.attributes.clear();
        this.attributes = null;
    }

    protected Future<Void> destroy(TaskRequest taskRequest) {
        log("释放");
        taskRequest.clearCache(this);
        return Future.succeededFuture();
    }

    protected Future<Void> hook(TaskRequest taskRequest) {
        return this.destroy(taskRequest).transform(event ->
                taskRequest.getChildrenRequest() == null ? (Future) CompositeFuture.join(Arrays.stream(taskRequest.getChildrenRequest()).map(taskRequest1 -> this.hook(taskRequest)).collect(Collectors.toList())) : Future.succeededFuture()
        );
    }

    public <T extends Unit> List<T> getChild(Class<T> tClass) {
        return (List) children.stream().filter(unit -> unit.getClass() == tClass).collect(Collectors.toList());
    }

    public <T extends Unit> List<T> getChild(Predicate<Unit> predicate) {
        return (List) children.stream().filter(predicate).collect(Collectors.toList());
    }


    protected <T extends Unit> List<T> getChildren(Class<T> tClass) {
        List<Unit> units = new LinkedList<>();
        units.addAll(getChild(tClass));
        for (Unit child : children) {
            units.addAll(child.getChildren(tClass));
        }
        return (List) units;
    }

    protected <T extends Unit> T getParent(Class<T> tClass) {
        return getParent(aClass -> aClass == tClass);
    }

    protected <T extends Unit> List<T> getParents(Class<T> tClass) {
        return getParents(aClass -> aClass == tClass);
    }

    protected <T extends Unit> T getParent(Predicate<Class<?>> predicate) {
        Class<? extends Unit> pClass = null;
        Unit target = this;
        do {
            target = target.parent;
            if (target == null) break;
            pClass = target.getClass();
        } while (!predicate.test(pClass));
        return (T) target;
    }

    protected <T extends Unit> List<T> getParents(Predicate<Class<?>> predicate) {
        List<T> list = new LinkedList<>();
        Unit t = this;
        while ((t = t.getParent(predicate)) != null) {
            list.add((T) t);
        }
        return list;
    }

    /*protected void execute(TaskRequest data) {
        execute(data, Object.class);
    }

    protected final void execute(TaskRequest data, Future<?> future, Class<?>... aClass) {
        Promise<Object> superPromise = data.promise;
        for (Unit child : children) {
            for (Class<?> aClass1 : aClass) {
                if (aClass1.isAssignableFrom(data.getClass())) {
                    future.compose(o -> Future.future(promise -> {
                        data.lastUnitResult = o;
                        data.setPromise(promise);
                        try {
                            child.execute(data);
                        } catch (Throwable throwable) {
                            promise.fail(throwable);
                        }
                    }));
                }

            }
        }
        future.onSuccess(superPromise::complete).onFailure(superPromise::fail);
    }

    protected final void execute(TaskRequest data, Class<?>... aClass) throws Throwable {
        execute(data, Future.succeededFuture(), aClass);
    }*/

    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        startChildUnit(data, /*data.lastUnitResult*/preUnitResult, Objects::nonNull).onSuccess(toNext::complete).onFailure(toNext::fail);
    }

    public static Predicate<Unit> typeMatched(Class<?>... classes) {
        return unit -> Arrays.stream(classes).filter(aClass -> aClass.isAssignableFrom(unit.getClass())).findFirst().isPresent();
    }

    Future<Object> startChildUnit(TaskRequest data, Object value, Predicate<Unit> predicate) {
        Future<Object> f = Future.succeededFuture(value);
        for (Unit child : children) {
            if (!predicate.test(child))
                continue;
            /*f = f.compose(o -> *//*{
                return Future.future(promise -> {
                    data.lastUnitResult = o;
                    child.start(data, promise);
                };
            }*//*prepareNextUnit(data, o, child));*/
            f = startChildUnit(child, data, f);
        }
        return f;
    }

    Future<Object> startChildUnit(Unit child, TaskRequest data, Future<Object> future) {
        /*Promise<Object> promise = Promise.promise();
        future.onComplete(event -> {
            if (event.failed()) {
                promise.fail(event.cause());
            } else {
                promise.complete(event.result());
            }
        });
        return promise.future().compose(o -> prepareNextUnit(data, o, child));*/
        return Iterable.compose(future, o -> startChildUnit(data, o, child));
    }

    protected Future<Object> startChildUnit(TaskRequest data, Object value, Unit unit) {
        /*final long[] cost = new long[1];
        return Future.future(promise -> {
            cost[0] = System.currentTimeMillis();
//            data.lastUnitResult = value;
            System.out.println();
            unit.log(String.format(">>>>>>>>>>开始[%s]>>>>>>>>>>>", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            unit.start(data, value, promise);
        }).onComplete(objectAsyncResult -> {
            if (objectAsyncResult.failed())
                unit.log(LogUtils.styleString("", 41, 3, "失败:" + objectAsyncResult.cause().getMessage()));
            unit.log(String.format("<<<<<<<<<<结束[%s][耗时:%s秒]<<<<<<<<<<<", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost[0]) / 1000.0d));
            System.out.println();
        });*/
        /*Promise<Object> promise = Promise.promise();
        long cost = System.currentTimeMillis();
        System.out.println();
        unit.log(String.format(">>>>>>>>>>开始[%s]>>>>>>>>>>>", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        unit.start(data, value, promise);

        return promise.future().onComplete(objectAsyncResult -> {
            if (objectAsyncResult.failed()) {
                objectAsyncResult.cause().printStackTrace();
                unit.log(LogUtils.styleString("", 41, 3, "失败:" + objectAsyncResult.cause().getMessage()));
            }
            unit.log(String.format("<<<<<<<<<<结束[%s][耗时:%s秒]<<<<<<<<<<<", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost) / 1000.0d));
            System.out.println();
        });*/
        long cost = System.currentTimeMillis();
        return executeBlocking(promise -> {
            System.out.println();
            unit.log(String.format(">>>>>>>>>>开始[%s]>>>>>>>>>>>", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            unit.start(data, value, promise);
        }).onComplete(objectAsyncResult -> {
            if (objectAsyncResult.failed())
                unit.log(LogUtils.styleString("", 41, 3, "失败:" + objectAsyncResult.cause().getMessage()));
            unit.log(String.format("<<<<<<<<<<结束[%s][耗时:%s秒]<<<<<<<<<<<", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost) / 1000.0d));
            System.out.println();
        });
    }

    protected synchronized void logNoTag(Object o) {
        System.out.print(o);
    }

    protected synchronized void logInline(Object o) {
        printTag(true);
        System.out.print(":");
        System.out.print(o);
    }

    protected synchronized void log(Object o) {
        logInline(o);
        System.out.println();
    }

    protected synchronized void printTag(boolean highLight) {
        if (this.parent != null) {
            this.parent.printTag(false);
        }
        String str = String.format("==>[%s%s]", this.getClass().getSimpleName(), !StringUtils.isEmpty(this.id) ? (":" + this.id) : "");
        System.out.print(highLight ? LogUtils.styleString("", (hashCode() % 5) + 31, 1, str) : str);
    }

    protected synchronized static <T extends Copyable> T copyableUnit(T source) {
        try {
            Unit target = (Unit) source.getClass().getConstructor().newInstance();
            target.id = ((Unit) source).id;
            target.attributes = ((Unit) source).attributes;
            ((Unit) source)
                    .children
                    .stream()
                    .filter(unit -> unit instanceof Copyable)
                    .forEach(unit -> {
                        Unit child = (Unit) ((Copyable) unit).copy();
                        child.setParent(target);
                    });
            return (T) target;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    final static String DEFAULT_VALUE_SIGN = "\\{\\{(.+?)\\}\\}";
    final static Pattern PATTERN = Pattern.compile(DEFAULT_VALUE_SIGN);
    final static String sign = "%@#VALUE#@%";

    protected static String textFormat(TaskRequest request, String text) {
        if (StringUtils.isEmpty(text)) return "";
        if (text.indexOf("{{") == -1) return text;
        List<Map.Entry<String, Object>> list = new LinkedList<>();
        Matcher matcher = PATTERN.matcher(text);
        String name;
        Map.Entry<String, Object> value;
        while (matcher.find()) {
            switch (matcher.groupCount()) {
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    continue;
            }
            try {
                value = Map.entry(name, Ognl.getValue(name, request.getOgnlContext(), request.getBus(), Object.class));
            } catch (Throwable e) {/*
                throw new RuntimeException(e.getMessage(), e);*/
                System.err.println(text + "===>" + name + "==>" + e.getMessage());
                value = Map.entry(name, "");
            }
            list.add(value);
        }
        text = text.replaceAll(DEFAULT_VALUE_SIGN, sign);
        int offset;
        for (Map.Entry<String, Object> entry : list) {
            offset = text.indexOf(sign);
            text = text.substring(0, offset) + TypeUtils.castToString(entry.getValue()) + text.substring(offset + sign.length());
//            text = text.replaceFirst(sign, TypeUtils.castToString(entry.getValue()));
        }
        return text;
    }

    protected String getAttrFormatValue(String name, TaskRequest data) {
        return textFormat(data, attributes.get(name));
    }

    protected static int countReset(AtomicInteger aLong, int triggerCount, int reset) {
        if (aLong.incrementAndGet() > triggerCount) {
            aLong.set(reset);
        }
        return aLong.get();
    }

    static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop) {
        return Iterable.compose(preFuture, loop);
    }

    static <T, K> Future<T> compose(Future<K> preFuture, Function<K, Future<T>> loop, Function<Throwable, Future<T>> failureMapper) {
        return Iterable.compose(preFuture, loop, failureMapper);
    }

    static void complete(AsyncResult asyncResult, Promise promise) {
        if (asyncResult.succeeded()) {
            promise.complete(asyncResult.result());
        } else {
            promise.fail(asyncResult.cause());
        }
    }

    protected static <T> Future<T> executeBlocking(Handler<Promise<T>> blockingCodeHandler) {
        return Task.getVertx().executeBlocking(blockingCodeHandler);
    }
}
