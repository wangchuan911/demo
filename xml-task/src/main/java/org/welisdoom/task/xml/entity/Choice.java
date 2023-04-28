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
        Future<Object> future = Future.succeededFuture();
        for (When when : getChild(When.class)) {
            future = future.compose(o ->
                            prepareNextUnit(data, preUnitResult, when)
                    ,
                    throwable -> {
                        if (throwable instanceof Break.BreakLoopThrowable) {
                            return Future.succeededFuture();
                        } else if (throwable instanceof Break.SkipOneLoopThrowable) {
                            return prepareNextUnit(data, preUnitResult, when);
                        } else
                            return Future.failedFuture(throwable);
                    });
        }
        for (Otherwise otherwise : getChild(Otherwise.class)) {
            future = prepareNextUnit(data, preUnitResult, otherwise);
            break;
        }
        if (future == null)
            future = Future.succeededFuture(preUnitResult);
        future.onSuccess(toNext::complete).onFailure(toNext::fail);
    }

    @Tag(value = "when", parentTagTypes = Choice.class, desc = "if else")
    public static class When extends Unit implements Executable {
        @Override
        protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
            boolean test;
            try {
                test = If.test(attributes.get("test"), data.getOgnlContext(), data.getBus());
            } catch (OgnlException e) {
                test = false;
            }
            log(String.format("表达式[%s]", attributes.get("test")));
            log(String.format("参数[%s]", data.getBus().toString()));
            log(String.format("结果[%s]", test));
            if (test) {
                Promise<Object> promise = Promise.promise();
                promise.future().onComplete(event -> {
                    if (event.failed()) {
                        toNext.fail(event.cause());
                    } else {
                        toNext.fail(new Break.BreakLoopThrowable(0));
                    }
                });
                super.start(data, preUnitResult, promise);
            } else
                toNext.fail(new Break.SkipOneLoopThrowable());
        }
    }

    @Tag(value = "otherwise", parentTagTypes = Choice.class, desc = "if else")
    public static class Otherwise extends Unit implements Executable {

    }
}
