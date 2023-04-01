package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Initialize;

import java.util.Map;

/**
 * @Classname Transactional
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "transactional", parentTagTypes = Executable.class)
public class Transactional extends Unit implements Executable {
    @Override
    protected Future<Object> execute(TaskRequest data) throws Throwable {
        try {
            return super.execute(data);
        } finally {
        }
    }
}
