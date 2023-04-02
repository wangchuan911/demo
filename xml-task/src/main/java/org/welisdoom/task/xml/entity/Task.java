package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.*;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class)
public class Task extends Unit implements Root {
    static Vertx vertx = Vertx.vertx();
    static Set<TaskRequest> tasks = new HashSet<>();

    public static Vertx getVertx() {
        return vertx;
    }

    public void run(TaskRequest data) {
        tasks.add(data);
        Promise<Object> promise = Promise.promise();
        start(data, promise);
        promise.future().onSuccess(o -> {
            System.out.println("success");
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
