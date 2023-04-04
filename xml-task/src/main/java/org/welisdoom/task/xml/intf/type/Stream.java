package org.welisdoom.task.xml.intf.type;

import io.vertx.core.Future;
import org.welisdoom.task.xml.entity.TaskRequest;


/**
 * @Classname Stream
 * @Description TODO
 * @Author Septem
 * @Date 9:39
 */
public interface Stream extends UnitType {

    Future<Object> read(TaskRequest request);

    Future<Object> writer(TaskRequest request);
}
