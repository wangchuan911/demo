package org.welisdoom.task.xml.entity;

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
            System.out.println("属性值：" + name + "=" + value);
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
        this.destroyBefore();
        for (Unit child : children) {
            child.destroy();
        }
        this.parent = null;
        this.children.clear();
        this.children = null;
        this.attributes.clear();
        this.attributes = null;
    }

    protected void destroyBefore() {

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

    protected void execute(TaskRequest data) {
        execute(data, Object.class);
    }

    protected final void execute(TaskRequest data, Class<?>... aClass) {
        for (Unit child : children) {
            for (Class<?> aClass1 : aClass) {
                if (aClass1.isAssignableFrom(data.getClass()))
                    child.execute(data);
                break;
            }
        }
    }
}
