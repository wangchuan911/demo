package org.welisdoom.task.xml.entity;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.LogUtils;
import org.welisdoon.common.data.IData;
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

    protected Future<Void> destroy(TaskInstance taskInstance) {
        log("释放");
        taskInstance.clearCache(this);
        return Future.succeededFuture();
    }

    protected Future<Void> hook(TaskInstance taskInstance) {
        return this.destroy(taskInstance).transform(event ->
                taskInstance.getChildrenRequest() == null ? (Future) CompositeFuture.join(Arrays.stream(taskInstance.getChildrenRequest()).map(taskRequest1 -> this.hook(taskInstance)).collect(Collectors.toList())) : Future.succeededFuture()
        );
    }

    public <T extends Unit> List<T> getChild(Class<T> tClass) {
        return (List) children.stream().filter(unit -> unit.getClass() == tClass).collect(Collectors.toList());
    }

    public <T extends Unit> List<T> getChild(Predicate<Unit> predicate) {
        return (List) children.stream().filter(predicate).collect(Collectors.toList());
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

    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        data.setPrevUnitValue(preUnitResult);
        return startChildUnit(data, preUnitResult, Objects::nonNull).onComplete(event -> {
            data.delPrevUnitValue();
        });
    }

    protected Future<Object> startChildUnit(TaskInstance data, Object value, Predicate<Unit> predicate) {
        Future<Object> f = Future.succeededFuture(value);
        for (Unit child : getChild(predicate)) {
            f = f.compose(o -> startChildUnit(data, o, child));
        }
        return f;
    }

    protected Future<Object> startChildUnit(TaskInstance data, Object value, Unit unit) {
        long cost = System.currentTimeMillis();
        return executeBlocking(promise -> {
            System.out.println();
            unit.log(String.format(">>>>>>>>>>开始[%s]>>>>>>>>>>>", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            try {
                unit.start(data, value).onComplete(promise);
            } catch (Throwable e) {
                promise.fail(e);
            }
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


    protected String getAttrFormatValue(String name, TaskInstance data) {
        return UnitType.textFormat(data, attributes.get(name));
    }


    protected static <T> Future<T> executeBlocking(Handler<Promise<T>> blockingCodeHandler) {
        return Task.getVertx().executeBlocking(blockingCodeHandler);
    }
}
