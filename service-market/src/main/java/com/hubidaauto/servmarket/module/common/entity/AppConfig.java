package com.hubidaauto.servmarket.module.common.entity;

/**
 * @Classname AppConfig
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/10/12 22:07
 */
public class AppConfig {
    String name,value,group;

    public String getName() {
        return name;
    }

    public AppConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AppConfig setValue(String value) {
        this.value = value;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public AppConfig setGroup(String group) {
        this.group = group;
        return this;
    }
}
