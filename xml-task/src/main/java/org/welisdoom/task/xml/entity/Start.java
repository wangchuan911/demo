package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;

/**
 * @Classname Start
 * @Description TODO
 * @Author Septem
 * @Date 20:20
 */
@Tag(value = "start", parentTagTypes = Root.class,desc = "任务执行")
public class Start extends Unit implements Executable {
}
