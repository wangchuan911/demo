package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.consts.MagicKey;
import org.welisdoom.task.xml.intf.type.Executable;

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
    @Override
    protected void startSync(TaskSession data) throws Throwable {
        super.startSync(data);
        SubTask.Config config = new SubTask.Config();
        SubTask.Mode mode = SubTask.Mode.valueOf(attributes.get("mode"));
        config.setMode(mode).setPath(attributes.get("mode"));
        for (String s : attributes.keySet()) {
            if (s.startsWith("params-")) {
                config.getParams().put(s.substring(7), attributes.get(s));
            }
        }
        Task.runSync(String.format("sub-task-%d-%d", System.currentTimeMillis(), (int) (Math.random() * 100)), config, data);
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
            task = Task.createTask(config);
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
