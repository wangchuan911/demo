package org.welisdoom.task.xml.entity;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag("task")
public class Task extends Unit {


    public Unit getInstances(String name) {
        return getChild(Initialization.class).get(0)
                .getChildren(Instance.class)
                .stream()
                .filter(instance -> Objects.equals(name, instance.getName()))
                .findFirst().orElse(null).getInstance();
    }


}
