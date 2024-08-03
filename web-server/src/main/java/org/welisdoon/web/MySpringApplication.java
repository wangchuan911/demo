package org.welisdoon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.util.Assert;
import org.welisdoon.common.LogUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MySpringApplication {
    static Class<? extends MySpringApplication> appClass;

    public static void run(Class<? extends MySpringApplication> aClass, String[] args) {
        setAppClass(aClass);
        SpringApplication.run(getAppClass(), args);
    }

    public static Class<? extends MySpringApplication> getAppClass() {
        return appClass;
    }

    protected static void setAppClass(Class<? extends MySpringApplication> appClass) {
        Assert.isNull(MySpringApplication.appClass, "此方法应用生命周期中只能执行一次！");
        MySpringApplication.appClass = appClass;
    }

    @PostConstruct
    void check() {
        if (appClass == null) return;
        Class aClass = ApplicationContextProvider.getRealClass(getClass());
        Assert.isTrue(appClass == aClass, getNoticeMsg(aClass));
    }

    protected String getNoticeMsg(Class aClass) {
        return "\n" + LogUtils.styleString("", 31, 1, String.format("启动时请剔除当前class[%s]", aClass.getName())) + "\n" +
                LogUtils.styleString("", 41, 1, "参考") + "\n" +
                LogUtils.styleString("", 31, 1, String.format(
                        "@org.springframework.context.annotation.ComponentScan(\n" +
                                "        basePackageClasses = {%s},\n" +
                                "        excludeFilters = {\n" +
                                "                @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {%s})\n" +
                                "        }\n" +
                                ")",
                        List.of(appClass, aClass, WebserverApplication.class)
                                .stream()
                                .distinct()
                                .map(aClass1 -> aClass1.getName() + ".class")
                                .collect(Collectors.joining(",")),
                        List.of(aClass, WebserverApplication.class)
                                .stream()
                                .distinct()
                                .map(aClass1 -> aClass1.getName() + ".class")
                                .collect(Collectors.joining(","))));
    }
}
