package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.ConditionHandler;
import org.welisdoom.task.xml.intf.DataHandler;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname Unit
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */
public class Unit {
    String name;
    Unit parent;
    List<Unit> children = new LinkedList<>();
    String conntent;

    public Unit setParent(Unit parent) {
        this.parent = parent;
        if (this.parent != null)
            this.parent.children.add(this);
        return this;
    }

    public Unit setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public Unit attr(Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(name);
            System.out.println("属性值：" + name + "=" + value);
        }
        return this;
    }

    public void nodeEnd() {
        if (parent != null)
            parent.nodeEndOnChild(this);

    }

    public void nodeEndOnChild(Unit unit) {

    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "{" +
                "name='" + name + '\'' +
                '}';
    }

    public void setConntent(String conntent) {
        this.conntent = conntent;
    }

    protected <T extends Unit> T getChild(Class<T> tClass) {
        return (T) children.stream().filter(unit -> unit.getClass() == tClass).findFirst().orElse(null);
    }

    protected void handler(Unit unit, Map<String, Object> data) {
        if (unit instanceof ConditionHandler) {
            ((ConditionHandler) unit).onCondition(data);
        } else if (unit instanceof DataHandler) {
            ((DataHandler) unit).onData(data);
        } else {
            for (Unit child : unit.children) {
                child.handler(child, data);
            }
        }
    }
}