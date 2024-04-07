package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceThrowable;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname Break
 * @Description TODO
 * @Author Septem
 * @Date 16:39
 */
@Tag(value = "break", parentTagTypes = Executable.class, desc = "停止循环")
@Attr(name = "deep", desc = "中断深度")
public class Break extends Unit implements Executable {
    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        return Future.failedFuture(new BreakLoopThrowable(MapUtils.getInteger(attributes, "deep", 1)));
    }

    public static class BreakLoopThrowable extends NoStackTraceThrowable {
        AtomicInteger deep = new AtomicInteger(1);

        BreakLoopThrowable(int deep) {
            super("中断");
            this.deep.set(deep);
        }

        public int decrementAndGetDeep() {
            return deep.decrementAndGet();
        }

        public int getDeep() {
            return deep.get();
        }
    }

    public static class SkipOneLoopThrowable extends NoStackTraceThrowable {

        SkipOneLoopThrowable() {
            super("跳过");
        }
    }

    public static Future<Object> onBreak(Throwable throwable, Object result) {
        if (throwable instanceof Break.BreakLoopThrowable) {
            if (((Break.BreakLoopThrowable) throwable).decrementAndGetDeep() == 0) {
                return Future.succeededFuture(result);
            }
        }
        return Future.failedFuture(throwable);
    }

    @Tag(value = "continue", parentTagTypes = Executable.class, desc = "跳过循环")
    public static class Continue extends Unit implements Executable {
        @Override
        protected Future<Object> start(TaskInstance data, Object preUnitResult) {
            return Future.failedFuture(new SkipOneLoopThrowable());
        }
    }

}
