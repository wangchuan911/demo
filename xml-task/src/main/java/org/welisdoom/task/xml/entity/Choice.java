package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import ognl.OgnlException;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.util.List;

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
        List<Unit> list = (List) getChild(When.class);
        list.add(getChild(Otherwise.class).stream().findFirst().orElse((Otherwise) new Otherwise().setParent(this)));
        for (Unit when : list) {
            future = compose(future,
                    o ->
                            startChildUnit(data, preUnitResult, when)
                    ,
                    throwable -> {
                        if (throwable instanceof Break.SkipOneLoopThrowable) {
                            return startChildUnit(data, preUnitResult, when);
                        } else
                            return Future.failedFuture(throwable);
                    });
        }
        future.onSuccess(toNext::complete).onFailure(event -> {
            if (event instanceof Break.BreakLoopThrowable) {
                toNext.complete();
            } else
                toNext.fail(event);
        });
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
            log(String.format("参数[%s]", JSON.toJSONString(data.getBus(), SerializerFeature.IgnoreErrorGetter, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty)));
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
