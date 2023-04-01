package org.welisdoom.task.xml.connect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Db
 * @Description TODO
 * @Author Septem
 * @Date 14:17
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Db {
    String value();
}
