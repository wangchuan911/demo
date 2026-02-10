package org.welisdoom.task.xml;

import io.vertx.core.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.welisdoom.task.xml.entity.SubTask;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoon.common.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname XmlTaslApplication
 * @Description TODO
 * @Author Septem
 * @Date 17:06
 */

@SpringBootApplication
//@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(basePackageClasses = {XmlTaskApplication.class}, excludeFilters = {
        @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {})
})
public class XmlTaskApplication {
    static Map<String, SubTask.Config> taskList = new LinkedHashMap<>();
    final static Pattern pattern = Pattern.compile("\\@task\\-(.+?)\\=(.+)"), pattern1 = Pattern.compile("\\@task\\-(.+?)\\-params\\-(.+?)\\=.+"), pattern2 = Pattern.compile("(\\w+):(.+)");
    static VertxOptions options = new VertxOptions();


    public static void main(String[] args) throws Throwable {
        List<String> newArgs = new LinkedList<>();
        String arg;
        String name;
        SubTask.Config config;
        List<String> matcher, matcher1;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (CollectionUtils.isNotEmpty(matcher = getValue(pattern.matcher(arg)))) {
                if (CollectionUtils.isNotEmpty(matcher1 = getValue(pattern1.matcher(arg)))) {
                    System.out.println(matcher1);
                    name = matcher1.get(0);
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new SubTask.Config());
                    config.getParams().put(matcher1.get(1), matcher.size() >= 2 ? matcher.get(1) : "");
                } else {
                    name = matcher.get(0);
                    config = ObjectUtils.getMapValueOrNewSafe(taskList, name, () -> new SubTask.Config());
                    List<String> list = getValue(pattern2.matcher(matcher.get(1)));
                    SubTask.Mode mode = SubTask.Mode.valueOf(list.get(0));
                    String xml = list.get(1);
                    config.setMode(mode).setPath(xml);
                }
                continue;
            } else
                newArgs.add(arg);
        }
        SpringApplication.run(XmlTaskApplication.class, newArgs.toArray(String[]::new));
    }

    static List<String> getValue(Matcher matcher) {
        List<String> list = new LinkedList<>();
        while (matcher.find()) {
            for (int i = 1, len = matcher.groupCount(); i <= len; i++) {
                list.add(matcher.group(i));
            }
        }
        return list;
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {

        if (Task.sync.get()) {
            Task.runSync(taskList);
        } else {
            options.setMaxWorkerExecuteTime(10);
            options.setMaxWorkerExecuteTimeUnit(TimeUnit.DAYS);
            options.setMaxEventLoopExecuteTime(10);
            options.setMaxEventLoopExecuteTimeUnit(TimeUnit.DAYS);
            DeploymentOptions deploymentOptions = new DeploymentOptions();
            deploymentOptions.setThreadingModel(ThreadingModel.WORKER);
            Vertx vertx = Vertx.vertx(options);
            vertx.deployVerticle(new AbstractVerticle() {
                @Override
                public void start() {
                    Task.setVertx(getVertx());
                    context.runOnContext(event1 -> {
                        Task.run(taskList, deploymentID());
                    });
                }
            }, deploymentOptions);
        }

    }
}
