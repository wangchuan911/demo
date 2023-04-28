package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
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
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        toNext.fail(new BreakLoopThrowable(MapUtils.getInteger(attributes, "deep", 1)));
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

    public static void onBreak(Throwable throwable, Promise<Object> toNext, Object result) {
        if (throwable instanceof Break.BreakLoopThrowable) {
            if (((Break.BreakLoopThrowable) throwable).decrementAndGetDeep() == 0) {
                toNext.complete(result);
                return;
            }
        }
        toNext.fail(throwable);
    }
}
