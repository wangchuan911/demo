package org.welisdoom.task.xml.entity;

import com.google.common.base.Throwables;
import io.vertx.core.Future;
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
    Final aFinal;

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        error = Optional.ofNullable(error).orElseGet(() -> {
            return getChild(Error.class).stream().findFirst().orElseGet(() -> (Error) new Error().setParent(this));
        });
        aFinal = Optional.ofNullable(aFinal).orElseGet(() -> {
            return getChild(Final.class).stream().findFirst().orElseGet(() -> (Final) new Final().setParent(this));
        });
        return startChildUnit(data, preUnitResult, unit -> !(unit instanceof Error)).compose(Future::succeededFuture, event -> {
            event.printStackTrace();
            return startChildUnit(data, Throwables.getStackTraceAsString(event), error);
        }).compose(o -> aFinal.start(data, o));
    }

    @Tag(value = "error", parentTagTypes = Catch.class, desc = "异常处理")
    @Attr(name = "throw", desc = "是否抛出")
    public static class Error extends Unit implements Executable {

    }

    @Tag(value = "final", parentTagTypes = Catch.class, desc = "最终处理")
    public static class Final extends Unit implements Executable {

    }
}
