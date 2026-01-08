package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.TaskSession;


/**
 * @Classname Stream
 * @Description TODO
 * @Author Septem
 * @Date 9:39
 */
public interface Stream<WRITER extends Stream.Writer> {

    @Deprecated
    Future<Object> read(TaskSession request);

    @Deprecated
    default Future<Object> write(TaskSession request) {
        return Future.succeededFuture();
    }

    @Deprecated
    Future<Object> write(TaskSession request, WRITER writer);

    interface Writer {

    }

    void readSync(TaskSession request) throws Throwable;

    default void writeSync(TaskSession request) throws Throwable {

    }

    void writeSync(TaskSession request, WRITER writer) throws Throwable;
}
