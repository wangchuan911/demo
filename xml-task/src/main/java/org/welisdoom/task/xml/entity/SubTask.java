package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.type.Executable;

import java.io.ByteArrayInputStream;
import java.io.File;
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

    public static Future<Object> run(String name, SubTask.Config config) {
        return run(name, config, null);
    }

    public static Future<Object> run(String name, SubTask.Config config, TaskInstance parent) {
        Promise<Object> promise = Promise.promise();
        Task task;
        try {
            switch (config.getMode()) {
                case classpath:
                    task = SAXParserHandler.loadTask(config.getPath());
                    break;
                case path:
                    task = SAXParserHandler.loadTask(new File(config.getPath()));
                    break;
                case db:
                    task = SAXParserHandler.loadTask(new ByteArrayInputStream(ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getTaskXML(Long.valueOf(config.getPath())).getBytes("utf-8")));
                    break;
                default:
                    throw new RuntimeException("未知的操作");
            }
            if (parent != null) {
                name = String.format("%s#%s", parent.id, name);
            }
            TaskInstance taskInstance = new TaskInstance(name, config.getParams());
            if (parent != null) {
                taskInstance.getBus().put("$parent", parent.getBus());
            }
            return task.run(taskInstance).compose(event -> {
                taskInstance.destroy();
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
