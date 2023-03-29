package org.welisdoom.task.xml.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag("task")
public class Task extends Unit {
    Map<String, Object> values = new HashMap<>();
    Map<String, Unit> instances = new HashMap<>();

    public Object getValues(String name) {
        return values.get(name);
    }

    public Unit getInstances(String name) {
        return instances.get(name);
    }

    public void setInstance(String name, Unit unit) {
        this.instances.put(name, unit);
    }

    public void setValue(String name, Object value) {
        this.values.put(name, value);
    }

    @Override
    public void nodeEnd() {
        System.out.println(values);
        System.out.println(instances);
    }
}
