package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.TaskRequest;


/**
 * @Classname Stream
 * @Description TODO
 * @Author Septem
 * @Date 9:39
 */
public interface Stream<WRITER extends Stream.Writer> extends UnitType {

    Future<Object> read(TaskRequest request);

    default Future<Object> write(TaskRequest request){
        return Future.succeededFuture();
    }

    Future<Object> write(TaskRequest request, WRITER writer);

    interface Writer extends UnitType {

    }
}
