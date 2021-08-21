package org.welisdoon.flow.module.template.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname NodeType
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/18 15:06
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeType {
    int value();
}
