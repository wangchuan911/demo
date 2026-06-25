package org.welisdoon.flow.module.flow.xml.tree.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname NodeType
 * @Description TODO
 * @Author Septem
 * @Date 17:42
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeType {
    String value();
}
