package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Root;

import java.util.*;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class, desc = "根节点")
public class Task extends Unit implements Root {
    final public static Vertx vertx = Vertx.vertx();
    static Set<TaskRequest> tasks = new HashSet<>();

    public static Vertx getVertx() {
        return vertx;
    }

    public Future<Object> run(TaskRequest data) {
        tasks.add(data);
        Promise<Object> promise = Promise.promise();
        start(data, promise);
        return promise.future().onSuccess(o -> {
            log("success");
        }).onFailure(
                throwable ->
                        throwable.printStackTrace())
                .onComplete(objectAsyncResult -> {
                    tasks.remove(data);
                    if (tasks.size() == 0)
                        vertx.close();
                });
//        v.close();

    }
}
