package org.welisdoon.flow.module.template.entity;

import org.reflections.ReflectionUtils;

/**
 * @Classname LinkFunction
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 17:06
 */
public class LinkFunction {
    Long id;
    String className;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public <T> Class<T> targetClass() {
        return (Class<T>) ReflectionUtils.forName(this.className, LinkFunction.class.getClassLoader());
    }
}
