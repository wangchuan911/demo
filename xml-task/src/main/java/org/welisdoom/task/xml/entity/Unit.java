package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoon.common.LogUtils;
import org.xml.sax.Attributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Classname Unit
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */
public class Unit implements BaseUnit<Unit> {
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

    @Deprecated
    protected Future<Void> destroy(TaskSession taskSession) {
        log("释放");
        taskSession.clearCache(this);
        return Future.succeededFuture();
    }

    @Deprecated
    protected Future<Void> hook(TaskSession taskSession) {
        return this.destroy(taskSession).transform(event ->
                Optional.ofNullable(taskSession.getChildrenRequest())
                        .map(taskInstances -> (Future) Future.join(Arrays.stream(taskSession.getChildrenRequest()).map(this::hook).collect(Collectors.toList())))
                        .orElseGet(Future::succeededFuture)
        );
    }

    protected void destroySync(TaskSession taskSession) {
        log("释放");
        taskSession.clearCache(this);
    }

    protected void hookSync(TaskSession taskSession) {
        this.destroySync(taskSession);
        Optional.ofNullable(taskSession.getChildrenRequest())
                .ifPresent(taskInstances -> {
                    for (TaskSession instance : taskSession.getChildrenRequest()) {
                        try {
                            this.hookSync(instance);
                        } catch (Throwable e) {
                            log(LogUtils.styleString("", 41, 3, "hook异常:" + e.getMessage()));
                        }
                    }
                });
    }

    public <T extends Unit> List<T> getChild(Class<T> tClass) {
        return (List) children.stream().filter(unit -> unit.getClass() == tClass).collect(Collectors.toList());
    }

    public <T extends Unit> List<T> getChild(Predicate<Unit> predicate) {
        return (List) children.<T>stream().filter(predicate).collect(Collectors.toList());
    }


    public <T extends Unit> List<T> getChildren(Class<T> tClass) {
        List<T> units = new LinkedList<>();
        units.addAll(getChild(tClass));
        for (Unit child : children) {
            units.addAll(child.getChildren(tClass));
        }
        return units;
    }

    public <T extends Unit> T getParent(Class<T> tClass) {
        return getParent(aClass -> aClass == tClass);
    }

    public <T extends Unit> List<T> getParents(Class<T> tClass) {
        return getParents(aClass -> aClass == tClass);
    }

    public <T extends Unit> T getParent(Predicate<Class<?>> predicate) {
        Class<? extends Unit> pClass;
        Unit target = this;
        do {
            target = target.parent;
            if (target == null) break;
            pClass = target.getClass();
        } while (!predicate.test(pClass));
        return (T) target;
    }

    public <T extends Unit> List<T> getParents(Predicate<Class<?>> predicate) {
        List<T> list = new LinkedList<>();
        Unit t = this;
        while ((t = t.getParent(predicate)) != null) {
            list.add((T) t);
        }
        return list;
    }

    @Deprecated
    protected Future<Object> start(TaskSession data, Object preUnitResult) {
        data.setPrevUnitValue(preUnitResult);
        return startChildUnit(data, preUnitResult, Objects::nonNull).onComplete(event -> {
            data.delPrevUnitValue();
        });
    }

    @Deprecated
    protected Future<Object> startChildUnit(TaskSession data, Object value, Predicate<Unit> predicate) {
        Future<Object> f = Future.succeededFuture(value);
        for (Unit child : getChild(predicate)) {
            f = f.compose(o -> startChildUnit(data, o, child));
        }
        return f;
    }

    protected void startSync(TaskSession data) throws Throwable {
        startChildUnitSync(data, Objects::nonNull);
    }

    protected void startChildUnitSync(TaskSession data, Predicate<Unit> predicate) throws Throwable {
        for (Unit child : getChild(predicate)) {
            startChildUnitSync(data, child);
        }
    }

    @Deprecated
    protected Future<Object> startChildUnit(TaskSession data, Object value, Unit unit) {
        long cost = System.currentTimeMillis();

        System.out.println();
        unit.log(String.format("_____________开始[%s]_____________", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), LogPosition.START);
        Future<Object> future;
        try {
            future = unit.start(data, value);
        } catch (Throwable e) {
            future = Future.failedFuture(e);
        }
        return future.onComplete(objectAsyncResult -> {
            if (objectAsyncResult.failed())
                unit.log(LogUtils.styleString("", 41, 3, "失败:" + objectAsyncResult.cause().getMessage()));
            unit.log(String.format("--------------结束[%s][耗时:%s秒]--------------", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost) / 1000.0d), LogPosition.END);
            System.out.println();
        });
    }

    protected void startChildUnitSync(TaskSession data, Unit unit) throws Throwable {
        long cost = System.currentTimeMillis();

        System.out.println();
        unit.log(String.format("_____________开始[%s]_____________", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), LogPosition.START);
        try {
            unit.startSync(data);
        } catch (Throwable e) {
            unit.log(LogUtils.styleString("", 41, 3, "失败:" + e.getMessage()));
            throw e;
        } finally {
            unit.log(String.format("--------------结束[%s][耗时:%s秒]--------------", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost) / 1000.0d), LogPosition.END);
            System.out.println();
        }
    }

    protected synchronized void logNoTag(Object o) {
        System.out.print(o);
    }

    protected synchronized void logInline(Object o, LogPosition mode) {
        printTag(true, mode);
        System.out.print(":");
        System.out.print(o);
    }

    protected synchronized void log(Object o) {
        logInline(o, LogPosition.MID);
        System.out.println();
    }

    protected synchronized void log(Object o, LogPosition mode) {
        logInline(o, mode);
        System.out.println();
    }

    protected void log(String str, Object... os) {
        for (int i = 0; i < os.length; i++) {
            if (os[i] == null)
                os[i] = "null";
        }
        this.log(String.format(str.replaceAll("\\{\\}", "%s"), os));
    }

    protected synchronized void printTag(boolean highLight, LogPosition mode) {
        if (this.parent != null) {
            this.parent.printTag(false, mode == LogPosition.END ? LogPosition.START : mode);
        }
        String str = "";
        if (this.parent != null) {
            switch (mode) {
                case START:
                    str = ">>";
                    break;
                case END:
                    str = "<<";
                    break;
                default:
                    str = "==";
                    break;
            }
        }

        str = String.format("%s[%s%s]", str, this.getClass().getSimpleName(), !StringUtils.isEmpty(this.id) ? (":" + this.id) : "");
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


    protected String getAttrFormatValue(String name, TaskSession data) {
        return BaseUnit.textFormat(data, attributes.get(name));
    }


    protected String getAttrOptions(String name) {
        return Optional.ofNullable(attributes.get(name)).orElseGet(() -> {
            Attr attr = Arrays.stream(
                    this.getClass().getAnnotations())
                    .filter(annotation -> annotation instanceof Attr && ((Attr) annotation).name().equals(name)).map(annotation -> (Attr) annotation).
                            findFirst().orElseThrow();
            return attr.options()[attr.defaultOption()];
        });
    }

    public enum LogPosition {
        START, MID, END
    }
}
