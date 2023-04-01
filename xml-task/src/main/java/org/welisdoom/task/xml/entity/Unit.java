package org.welisdoom.task.xml.entity;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    protected Future<Object> execute(TaskRequest data) throws Throwable {
        return execute(data, Object.class);
    }

    protected final Future<Object> execute(TaskRequest data, Class<?>... aClass) throws Throwable {
        Future future = Future.succeededFuture();
        for (Unit child : children) {
            for (Class<?> aClass1 : aClass) {
                if (aClass1.isAssignableFrom(data.getClass())) {
                    future.compose(o -> {
                        try {
                            return child.execute(data);
                        } catch (Throwable e) {
                            return Future.failedFuture(e);
                        }
                    });
                }

            }
        }
        return future;
    }
}
