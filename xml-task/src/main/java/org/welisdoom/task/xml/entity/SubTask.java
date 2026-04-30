package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.consts.MagicKey;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.handler.XmlParserHandler;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.type.Executable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname SubTask
 * @Description TODO
 * @Author Septem
 * @Date 10:22
 */
@Tag(value = "sub-task", parentTagTypes = Executable.class, desc = "任务子执行")
public class SubTask extends Unit implements Executable {

    public static void runSync(String name, SubTask.Config config) throws Throwable {
        runSync(name, config, null);
    }

    public static Task createTask(SubTask.Config config) throws Throwable {
        Task task;
        switch (config.getMode()) {
            case classpath:
                task = XmlParserHandler.loadTask(config.getPath());
                break;
            case path:
                task = XmlParserHandler.loadTask(new File(config.getPath()));
                break;
            case db:
                task = XmlParserHandler.loadTask(Long.parseLong(config.getPath()));
                break;
            default:
                throw new RuntimeException("未知的操作");
        }
        return task;
    }

    public static void runSync(String name, SubTask.Config config, TaskSession parent) throws Throwable {
        Task task = createTask(config);
        if (parent != null) {
            name = String.format("%s#%s", parent.id, name);
        }
        TaskSession taskSession = new TaskSession(name, config.getParams());
        if (parent != null) {
            taskSession.getBus().put(MagicKey.PARENT, parent.getBus());
        }
        try {
            task.runSync(taskSession);
        } finally {
            taskSession.destroySync();
        }

    }

    @Deprecated
    public static Future<Object> run(String name, SubTask.Config config) {
        return run(name, config, null);
    }

    @Deprecated
    public static Future<Object> run(String name, SubTask.Config config, TaskSession parent) {
        Promise<Object> promise = Promise.promise();
        Task task;
        try {
            task = createTask(config);
            if (parent != null) {
                name = String.format("%s#%s", parent.id, name);
            }
            TaskSession taskSession = new TaskSession(name, config.getParams());
            if (parent != null) {
                taskSession.getBus().put(MagicKey.PARENT, parent.getBus());
            }
            return task.run(taskSession).compose(event -> {
                taskSession.destroy();
                return Future.succeededFuture(event);
            });
        } catch (Throwable e) {
            e.printStackTrace();
            promise.fail(e);
        }
        return promise.future();
    }


    public static class Config {
        Map<String, Object> params = new HashMap<>();
        String path;
        Mode mode;


        public Map<String, Object> getParams() {
            return params;
        }

        public Config setParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public String getPath() {
            return path;
        }

        public Config setPath(String path) {
            this.path = path;
            return this;
        }

        public Mode getMode() {
            return mode;
        }

        public Config setMode(Mode mode) {
            this.mode = mode;
            return this;
        }
    }

    public enum Mode {
        classpath, path, db
    }
}
