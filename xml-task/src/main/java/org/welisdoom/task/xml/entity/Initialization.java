package org.welisdoom.task.xml.entity;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname Init
 * @Description TODO
 * @Author Septem
 * @Date 18:24
 */
@Tag(value = "initialization", parentTag = Task.class)
public class Initialization extends Unit {

    public static Initialization getInstance(Task task) {
        return task.getChild(Initialization.class).get(0);
    }
}
