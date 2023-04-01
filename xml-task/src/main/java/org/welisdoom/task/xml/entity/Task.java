package org.welisdoom.task.xml.entity;


import io.vertx.core.Vertx;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class)
public class Task extends Unit implements Root {
    public void run(TaskRequest data) {
        Vertx v = Vertx.vertx();
        v.executeBlocking(promise -> {
            try {
                data.setPromise(promise);
                execute(data);
            } catch (Throwable e) {
                e.printStackTrace();
                promise.fail(e);
            }
        }).onSuccess(o -> {
            System.out.println("success");
        }).onFailure(
                throwable ->
                        throwable.printStackTrace())
                .onComplete(objectAsyncResult -> {
                    v.close();
                });
//        v.close();

    }
}
