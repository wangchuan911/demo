package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import ognl.OgnlException;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

/**
 * @Classname Chooce
 * @Description TODO
 * @Author Septem
 * @Date 22:30
 */
@Tag(value = "choice", parentTagTypes = Executable.class, desc = "if else")
public class Choice extends Unit {

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        Future<Object> future = null;
        for (When when : getChild(When.class)) {
            if (when.match(data)) {
                future = startChildUnit(data, preUnitResult, unit -> unit.equals(when));
                break;
            }
        }
        for (Otherwise otherwise : getChild(Otherwise.class)) {
            future = startChildUnit(data, preUnitResult, unit -> unit.equals(otherwise));
            break;
        }
        if (future == null)
            future = Future.succeededFuture(preUnitResult);
        future.onSuccess(toNext::complete).onFailure(toNext::fail);
    }

    @Tag(value = "when", parentTagTypes = Choice.class, desc = "if else")
    public static class When extends Unit implements Executable {
        protected boolean match(TaskRequest data) {
            try {
                return If.test(attributes.get("test"), data.getOgnlContext(), data.getBus());
            } catch (OgnlException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Tag(value = "otherwise", parentTagTypes = Choice.class, desc = "if else")
    public static class Otherwise extends Unit implements Executable {

    }
}
