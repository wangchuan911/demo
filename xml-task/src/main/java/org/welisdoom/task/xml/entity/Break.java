package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoom.task.xml.intf.type.UnitType;

/**
 * @Classname Break
 * @Description TODO
 * @Author Septem
 * @Date 16:39
 */
@Tag(value = "break", parentTagTypes = Executable.class, desc = "停止循环")
public class Break extends Unit implements Executable {
    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        toNext.fail(new BreakLoopException());
    }

    public static class BreakLoopException extends RuntimeException {

    }
}
