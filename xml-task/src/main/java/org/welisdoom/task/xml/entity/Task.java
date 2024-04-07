package org.welisdoom.task.xml.entity;


import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.util.Assert;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Root;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
    static Set<TaskInstance> tasks = new HashSet<>();

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
        vertx.setPeriodic(10000, event -> {
            System.out.println(String.format("%s%s%s", s, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), s));
        });
    }

    public synchronized Future run(TaskInstance data) {
        tasks.add(data);
        try {
            return start(data, null)
                    .onSuccess(o -> {
                        log("success");
                    })
                    .onFailure(
                            throwable -> {
                                log("fail");
                                throwable.printStackTrace();
                            })
                    .transform(objectAsyncResult -> {
                        tasks.remove(data);
                        return CompositeFuture
                                .all(data.cache.entrySet()
                                        .stream()
                                        .map(unitObjectEntry -> {
                                            unitObjectEntry.getKey().log("开始释放");
                                            return unitObjectEntry.getKey()
                                                    .destroy(data).onComplete(event -> {
                                                        if (event.succeeded()) {
                                                            unitObjectEntry.getKey().log("释放完成");
                                                        } else {
                                                            unitObjectEntry.getKey().log("释放失败:");
                                                            event.cause().printStackTrace();
                                                        }
                                                    });
                                        })
                                        .collect(Collectors.toList()));
                    });
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    public static Future<Void> closeVertx() {
        if (vertx == null) return Future.failedFuture("vertx is null");
        return vertx.close();
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                CompositeFuture
                        .all(new HashSet<>(tasks).stream()
                                .map(taskRequest -> {
                                    tasks.remove(taskRequest);
                                    return CompositeFuture.join(taskRequest.cache.entrySet().stream()
                                            .map(unitObjectEntry -> {
                                                unitObjectEntry.getKey().log("开始销毁");
                                                return unitObjectEntry.getKey()
                                                        .hook(taskRequest).onComplete(event -> {
                                                            if (event.succeeded()) {
                                                                unitObjectEntry.getKey().log("销毁完成");
                                                            } else {
                                                                unitObjectEntry.getKey().log("销毁失败:");
                                                                event.cause().printStackTrace();
                                                            }
                                                        });
                                            })
                                            .collect(Collectors.toList()));
                                })
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
