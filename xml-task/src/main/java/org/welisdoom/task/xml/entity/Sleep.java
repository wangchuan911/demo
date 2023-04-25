package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

/**
 * @Classname Sleep
 * @Description TODO
 * @Author Septem
 * @Date 9:39
 */

@Tag(value = "sleep", parentTagTypes = {Executable.class}, desc = "阻塞线程")
@Attr(name = "time", desc = "阻塞时间", require = true)
public class Sleep extends Unit implements Executable {
    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        try {
            long time = MapUtils.getLong(attributes, "time");
            log("阻塞:" + time + "ms");
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.start(data, toNext);
    }
}
