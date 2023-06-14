package org.welisdoom.task.xml.entity;


import io.vertx.core.*;
import org.springframework.util.Assert;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Root;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class, desc = "根节点")
public class Task extends Unit implements Root {
    private static Vertx vertx;
    static Set<TaskRequest> tasks = new HashSet<>();

    public static Vertx getVertx() {
        return vertx;
    }

    public static void setVertxOption(VertxOptions options) {
        Assert.isNull(vertx, "vertx is created!");
        vertx = Vertx.vertx(options);
        longTimeNotice();
    }

    public static void longTimeNotice() {
        String s = Arrays.stream(new Character[10]).map(character -> "-").collect(Collectors.joining("-"));
        vertx.setPeriodic(1000, event -> {
            System.out.println(String.format("%s%s%s", s, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), s));
        });
    }

    public synchronized Future<Object> run(TaskRequest data) {
        tasks.add(data);
        Promise<Object> promise = Promise.promise();
        try {
            start(data, null, promise);
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future()
                .onSuccess(o -> {
                    log("success");
                })
                .onFailure(
                        throwable -> {
                            log("fail");
                            throwable.printStackTrace();
                        })
                .onComplete(objectAsyncResult -> {
                    tasks.remove(data);
                    CompositeFuture
                            .all(data.cache.entrySet()
                                    .stream()
                                    .map(unitObjectEntry -> unitObjectEntry
                                            .getKey()
                                            .destroy(data))
                                    .collect(Collectors.toList()))
                            .onComplete(event -> {
                                if (tasks.size() == 0)
                                    vertx.close();
                            });

                });
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                CompositeFuture
                        .all(tasks.stream()
                                .map(taskRequest -> CompositeFuture.all(taskRequest.cache.entrySet().stream()
                                        .map(unitObjectEntry -> unitObjectEntry.getKey()
                                                .hook(taskRequest))
                                        .collect(Collectors.toList())))
                                .collect(Collectors.toList())).onComplete(event -> {
                    if (Task.getVertx() != null)
                        Task.getVertx().close().onSuccess(unused -> {
                            System.out.println("vertx 停止");
                        }).onFailure(throwable -> {
                            System.out.println("vertx 失败");
                            throwable.printStackTrace();
                        });
                });
            }
        });
    }
}
