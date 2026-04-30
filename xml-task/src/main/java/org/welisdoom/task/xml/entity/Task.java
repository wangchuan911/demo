package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.springframework.util.Assert;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.XmlParserHandler;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoon.common.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Classname Task
 * @Description TODO
 * @Author Septem
 * @Date 19:03
 */
@Tag(value = "task", parentTagTypes = Root.class, desc = "根节点")
public class Task extends Unit implements Root {

    final static AtomicReference<Vertx> vertx = new AtomicReference<>();
    static Set<TaskSession> tasks = new HashSet<>();
    public final static AtomicBoolean sync = new AtomicBoolean(true);
    static Timer timer;

    public static Vertx getVertx() {
        return vertx.get();
    }

    public static void setVertx(Vertx vertx1) {
        Assert.notNull(vertx1, "vertx is created!");
        vertx.set(vertx1);
        longTimeNotice();
    }

    public static void longTimeNotice() {
        String s = Arrays.stream(new Character[10]).map(character -> "-").collect(Collectors.joining("-"));
        getVertx().setPeriodic(10000, event -> {
            System.out.println(String.format("%s%s%s", s, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), s));
        });
    }

    public synchronized void runSync(TaskSession data) throws Throwable {
        try {
            tasks.add(data);
            startSync(data);
            log("success");
        } catch (Throwable e) {
            log("fail");
            e.printStackTrace();
            throw e;
        } finally {
            tasks.remove(data);
            doAndChildrenDo(this, (unit) -> {
                try {
                    unit.destroySync(data);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            if (tasks.size() == 0) {
                timer.cancel();
            }
        }
    }

    protected static void doAndChildrenDo(Unit unit, Consumer<Unit> consumer) {
        ObjectUtils.find(
                unit,
                unit1 -> unit1.children,
                unit1 -> {
                    consumer.accept(unit1);
                    return true;
                });
    }

    public static void runSync(Map<String, SubTask.Config> taskList) {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                final String s = Arrays.stream(new Character[10]).map(character -> "-").collect(Collectors.joining("-"));

                @Override
                public void run() {
                    System.out.println(String.format("%s%s%s", s, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), s));
                }
            }, 10000, 10000);
        }
        for (Map.Entry<String, SubTask.Config> entry : taskList.entrySet()) {
            try {
                SubTask.runSync(entry.getKey(), entry.getValue());
                System.out.println("成功：" + entry.getKey());
            } catch (Throwable e) {
                System.out.println("失败：" + entry.getKey());
            }
        }
    }

    public static void run(Map<String, SubTask.Config> taskList, String taskId) {
        Future.join(taskList.entrySet().stream().map(entry ->
                SubTask.run(entry.getKey(), entry.getValue()).onComplete(event -> {
                    if (event.succeeded()) {
                        System.out.println("成功：" + entry.getKey());
                    } else {
                        System.out.println("失败：" + entry.getKey());
                    }
                })).collect(Collectors.toList())).onComplete(event -> {
            Task.closeVertx();
            getVertx().undeploy(taskId);
        });
    }

    @Deprecated
    public synchronized Future run(TaskSession data) {
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
                        return Future
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

    @Deprecated
    public static Future<Void> closeVertx() {
        if (getVertx() == null) return Future.failedFuture("vertx is null");
        return getVertx().close();
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (sync.get()) {
                    XmlParserHandler.TASK_MAP.forEach((s, task) -> {
                        doAndChildrenDo(task, Unit::hookSync);
                    });
                } else
                    Future
                            .all(new HashSet<>(tasks).stream()
                                    .map(taskRequest -> {
                                        tasks.remove(taskRequest);
                                        return Future.join(taskRequest.cache.entrySet().stream()
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
