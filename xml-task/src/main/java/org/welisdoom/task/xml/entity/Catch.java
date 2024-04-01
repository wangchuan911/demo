package org.welisdoom.task.xml.entity;

import com.google.common.base.Throwables;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

/**
 * @Classname Catch
 * @Description TODO
 * @Author Septem
 * @Date 22:29
 */
@Tag(value = "catch", parentTagTypes = Executable.class, desc = "捕获异常")
public class Catch extends Unit implements Executable {
    Error error;

    @Override
    protected void start(TaskInstance data, Object preUnitResult, Promise<Object> toNext) {
        if (error == null)
            error = getChild(Error.class).stream().findFirst().orElse((Error) new Error().setParent(this));
        startChildUnit(data, preUnitResult, unit -> !(unit instanceof Error)).onSuccess(toNext::complete).onFailure(event -> {
            event.printStackTrace();
            startChildUnit(data, Throwables.getStackTraceAsString(event), error).onSuccess(toNext::complete).onFailure(toNext::fail);
        });
    }

    @Tag(value = "error", parentTagTypes = Catch.class, desc = "异常处理")
    @Attr(name = "throw", desc = "是否抛出")
    public static class Error extends Unit implements Executable {
        @Override
        protected void start(TaskInstance data, Object preUnitResult, Promise<Object> toNext) {
            super.start(data, preUnitResult, toNext);
        }
    }
}
