package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.vertx.core.Future;
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
    protected void startSync(TaskSession data) throws Throwable {
        Otherwise otherwise = null;
        for (Unit unit : getChild(unit -> unit instanceof When || unit instanceof Otherwise)) {
            if (unit instanceof Otherwise) {
                otherwise = (Otherwise) unit;
            } else if (unit instanceof When) {
                try {
                    startChildUnitSync(data, unit);
                } catch (Break.SkipOneLoopThrowable e) {
                    continue;
                } catch (Break.BreakLoopThrowable e) {
                    Break.onBreak(e);
                    return;
                }
            }
        }
        if (otherwise != null) startChildUnitSync(data, otherwise);
    }

    @Override
    protected Future<Object> start(TaskSession data, Object preUnitResult) {
        Future<Object> future = Future.succeededFuture();
        List<Unit> list = (List) getChild(When.class);
        list.add(getChild(Otherwise.class).stream().findFirst().orElse((Otherwise) new Otherwise().setParent(this)));
        for (Unit when : list) {
            future = future.compose(
                    o -> startChildUnit(data, preUnitResult, when),
                    throwable -> {
                        if (throwable instanceof Break.SkipOneLoopThrowable) {
                            return startChildUnit(data, preUnitResult, when);
                        } else
                            return Future.failedFuture(throwable);
                    });
        }
        return future.otherwise(event -> {
            if (event instanceof Break.BreakLoopThrowable) {
                return Future.succeededFuture();
            } else
                return Future.failedFuture(event);
        });
    }

    @Tag(value = "when", parentTagTypes = Choice.class, desc = "if else")
    public static class When extends Unit implements Executable {
        @Override
        protected void startSync(TaskSession data) {
            boolean test;
            try {
                test = If.test(attributes.get("test"), data.getOgnlContext(), data.getBus());
            } catch (Throwable e) {
                e.printStackTrace();
                test = false;
            }
            log(String.format("表达式[%s]", attributes.get("test")));
            log(String.format("参数[%s]", JSON.toJSONString(data.getBus(), SerializerFeature.IgnoreErrorGetter, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty)));
            log(String.format("结果[%s]", test));
            throw test ? new Break.BreakLoopThrowable(0) : new Break.SkipOneLoopThrowable();
        }

        @Override
        protected Future<Object> start(TaskSession data, Object preUnitResult) {
            boolean test;
            try {
                test = If.test(attributes.get("test"), data.getOgnlContext(), data.getBus());
            } catch (Throwable e) {
                e.printStackTrace();
                test = false;
            }
            log(String.format("表达式[%s]", attributes.get("test")));
            log(String.format("参数[%s]", JSON.toJSONString(data.getBus(), SerializerFeature.IgnoreErrorGetter, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty)));
            log(String.format("结果[%s]", test));
            if (test) {
                return super.start(data, preUnitResult).compose(o -> {
                    return Future.failedFuture(new Break.BreakLoopThrowable(0));
                }, Future::failedFuture);
            } else
                return Future.failedFuture(new Break.SkipOneLoopThrowable());
        }
    }

    @Tag(value = "otherwise", parentTagTypes = Choice.class, desc = "if else")
    public static class Otherwise extends Unit implements Executable {

    }
}
