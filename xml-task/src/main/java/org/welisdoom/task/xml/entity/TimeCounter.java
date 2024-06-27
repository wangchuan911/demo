package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

/**
 * @Classname CountDownLatch
 * @Description TODO
 * @Author Septem
 * @Date 18:33
 */
@Tag(value = "time-counter", parentTagTypes = Executable.class, desc = "csv文件读写")
public class TimeCounter extends Unit implements Executable {
    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        long l = System.currentTimeMillis();
        return super.start(data, preUnitResult).compose(o -> {
            String s = timeFormat(System.currentTimeMillis() - l);
            log("耗时：{}", s);
            return Future.succeededFuture(s);
        });
    }

    static String timeFormat(long l) {
        StringBuilder builder = new StringBuilder();
        builder.append(l % 1000).append("ms");
        builder.insert(0, "s").insert(0, (l /= 1000) % 60);
        builder.insert(0, "m").insert(0, (l /= 60) % 60);
        builder.insert(0, "h").insert(0, (l /= 60) % 24);
        builder.insert(0, "d").insert(0, (l / 24));
        return builder.toString();
    }
}
