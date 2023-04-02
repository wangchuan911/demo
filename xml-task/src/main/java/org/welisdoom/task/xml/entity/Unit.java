package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.util.CollectionUtils;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname Unit
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */
public class Unit {
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
        return this;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "{" +
                "name='" + id + '\'' +
                '}';
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

    public void destroy(TaskRequest taskRequest) {

    }

    protected <T extends Unit> List<T> getChild(Class<T> tClass) {
        return (List) children.stream().filter(unit -> unit.getClass() == tClass).collect(Collectors.toList());
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
        Class<? extends Unit> pClass = null;
        Unit target = this;
        do {
            target = target.parent;
            pClass = target.getClass();
        } while (pClass != tClass);
        return (T) target;
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

    protected void start(TaskRequest data, Promise<Object> toNext) {
        startChildUnit(data, data.lastUnitResult, UnitType.class).onSuccess(toNext::complete).onFailure(toNext::fail);
    }


    protected Future<Object> startChildUnit(TaskRequest data, Object value, Class<?>... classes) {
        Future<Object> f = Future.succeededFuture(value), f1;
        for (Unit child : children) {
            if (Arrays.stream(classes).filter(aClass -> aClass.isAssignableFrom(child.getClass())).findFirst().isEmpty())
                continue;
            f = f.compose(o -> {
                return Future.future(promise -> {
                    data.lastUnitResult = o;
                    child.start(data, promise);
                });
            });

        }
        return f;
    }

    protected Future<Object> prepareNextUnit(TaskRequest data, Object value, Unit unit) {
        return Future.future(promise -> {
            data.lastUnitResult = value;
            unit.start(data, promise);
        });
    }

}
