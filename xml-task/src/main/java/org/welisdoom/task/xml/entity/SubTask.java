package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;

/**
 * @Classname SubTask
 * @Description TODO
 * @Author Septem
 * @Date 10:22
 */
@Tag(value = "sub-task", parentTagTypes = Executable.class, desc = "任务子执行")
public class SubTask extends Unit implements Executable {
    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        super.start(data, toNext);
    }
}
