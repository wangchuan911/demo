package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.type.UnitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Tag
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tag {
    String value();

    Class<? extends UnitType>[] parentTagTypes();
}
