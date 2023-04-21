package org.welisdoom.task.xml;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.formula.functions.T;
import org.mybatis.spring.annotation.MapperScan;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
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
    static Map<String, Config> taskList = new LinkedHashMap<>();
    final static String KEY_TASK_INDEX = "@task-", KEY_PARAMS = "-params-";

    public static void main(String[] args) throws Throwable {
        List<String> newArgs = new LinkedList<>();
        String arg;
        String name;
        Config config;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg.startsWith(KEY_TASK_INDEX)) {
                if (arg.indexOf(KEY_PARAMS) > 0) {
                    name = arg.substring(6, arg.indexOf(KEY_PARAMS));
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new Config());
                    config.params.put(
                            arg.substring(arg.indexOf(KEY_PARAMS, KEY_TASK_INDEX.length()) + KEY_PARAMS.length(), arg.indexOf("=", name.length() + KEY_PARAMS.length())),
                            arg.substring(arg.indexOf("=", name.length() + KEY_PARAMS.length()) + 1));
                } else {
                    name = arg.substring(6, arg.indexOf("="));
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new Config());
                    Mode mode = Mode.valueOf(arg.substring(KEY_TASK_INDEX.length() + name.length() + 1, arg.indexOf(":", name.length())));
                    String xml = arg.substring(arg.indexOf(":", name.length()) + 1);
                    config.setMode(mode).setXmlPath(xml);
                }
                continue;
            } else
                newArgs.add(arg);
        }
        SpringApplication.run(XmlTaskApplication.class, newArgs.toArray(String[]::new));
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        for (Map.Entry<String, Config> stringStringEntry : Map.copyOf(taskList).entrySet()) {
            Task.vertx.executeBlocking(promise -> {
                Task task;
                Config config = stringStringEntry.getValue();
                try {
                    switch (config.mode) {
                        case classpath:
                            task = SAXParserHandler.loadTask(config.xmlPath);
                            break;
                        case path:
                            task = SAXParserHandler.loadTask(new File(config.xmlPath));
                            break;
                        case db:
                            task = SAXParserHandler.loadTask(new ByteArrayInputStream(ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getTaskXML(config.xmlPath).getBytes("utf-8")));
                            break;
                        default:
                            promise.fail("未知的操作");
                            return;
                    }
                    task.run(new TaskRequest(config.params))
                            .onSuccess(promise::complete).onFailure(promise::fail);
                } catch (Throwable e) {
                    e.printStackTrace();
                    promise.fail(e);
                }
            });
        }
    }

    static class Config {
        Map<String, Object> params = new HashMap<>();
        String xmlPath;
        Mode mode;


        public Map<String, Object> getParams() {
            return params;
        }

        public Config setParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public String getXmlPath() {
            return xmlPath;
        }

        public Config setXmlPath(String xmlPath) {
            this.xmlPath = xmlPath;
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

    enum Mode {
        classpath, path, db
    }
}
