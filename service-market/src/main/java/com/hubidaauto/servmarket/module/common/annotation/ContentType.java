package com.hubidaauto.servmarket.module.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname ContentType
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/17 23:44
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentType {
    long id();

    String table();

}
