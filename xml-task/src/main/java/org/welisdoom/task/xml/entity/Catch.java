package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.util.Optional;

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
    protected void start(TaskRequest data, Promise<Object> toNext) {
        if (error == null)
            error = getChild(Error.class).stream().findFirst().orElse((Error) new Error().setParent(this));
        startChildUnit(data, data.getLastUnitResult(), unit -> !(unit instanceof Error)).onSuccess(toNext::complete).onFailure(event -> {
            prepareNextUnit(data, event, error).onSuccess(toNext::complete).onFailure(toNext::fail);
        });
    }

    @Tag(value = "error", parentTagTypes = Catch.class, desc = "异常处理")
    @Attr(name = "throw", desc = "是否抛出")
    public static class Error extends Unit implements Executable {
        @Override
        protected void start(TaskRequest data, Promise<Object> toNext) {
            super.start(data, toNext);
        }
    }
}
