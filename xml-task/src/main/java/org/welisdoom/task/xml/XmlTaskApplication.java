package org.welisdoom.task.xml;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.SubTask;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

/**
 * @Classname XmlTaslApplication
 * @Description TODO
 * @Author Septem
 * @Date 17:06
 */

@SpringBootApplication
//@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackageClasses = XmlTaskApplication.class, annotationClass = Repository.class)
@ComponentScan(basePackageClasses = {WebserverApplication.class, XmlTaskApplication.class})
public class XmlTaskApplication {
    static Map<String, SubTask.Config> taskList = new LinkedHashMap<>();
    final static String KEY_TASK_INDEX = "@task-", KEY_PARAMS = "-params-";

    public static void main(String[] args) throws Throwable {
        List<String> newArgs = new LinkedList<>();
        String arg;
        String name;
        SubTask.Config config;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg.startsWith(KEY_TASK_INDEX)) {
                if (arg.indexOf(KEY_PARAMS) > 0) {
                    name = arg.substring(6, arg.indexOf(KEY_PARAMS));
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new SubTask.Config());
                    config.getParams().put(
                            arg.substring(arg.indexOf(KEY_PARAMS, KEY_TASK_INDEX.length()) + KEY_PARAMS.length(), arg.indexOf("=", name.length() + KEY_PARAMS.length())),
                            arg.substring(arg.indexOf("=", name.length() + KEY_PARAMS.length()) + 1));
                } else {
                    name = arg.substring(6, arg.indexOf("="));
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new SubTask.Config());
                    SubTask.Mode mode = SubTask.Mode.valueOf(arg.substring(KEY_TASK_INDEX.length() + name.length() + 1, arg.indexOf(":", name.length())));
                    String xml = arg.substring(arg.indexOf(":", name.length()) + 1);
                    config.setMode(mode).setPath(xml);
                }
                continue;
            } else
                newArgs.add(arg);
        }
        SpringApplication.run(XmlTaskApplication.class, newArgs.toArray(String[]::new));
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        for (Map.Entry<String, SubTask.Config> stringStringEntry : Map.copyOf(taskList).entrySet()) {
            Task.vertx.executeBlocking(promise -> {
                SubTask.run(stringStringEntry.getKey(), stringStringEntry.getValue());
            });
        }
    }


}
