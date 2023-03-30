package org.welisdoom.task.xml.entity;


import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class)
public class Task extends Unit implements Root {
    public void run(Map<String, Object> data) {
        execute(data);
    }
}
