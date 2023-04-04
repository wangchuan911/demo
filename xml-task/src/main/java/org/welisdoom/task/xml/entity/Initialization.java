package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.Root;

/**
 * @Classname Init
 * @Description TODO
 * @Author Septem
 * @Date 18:24
 */
@Tag(value = "initialization", parentTagTypes = Root.class, desc = "初始化，所有的初始话信息都要写到此标签")
public class Initialization extends Unit implements Initialize {

    public static Initialization getInstance(Task task) {
        return task.getChild(Initialization.class).get(0);
    }
}
