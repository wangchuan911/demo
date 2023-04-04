package org.welisdoom.task.xml.annotations;

import java.lang.annotation.*;

/**
 * @Classname Attr
 * @Description TODO
 * @Author Septem
 * @Date 16:42
 */
@Repeatable(Attrs.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attr {
    String name();

    String desc() default "";

    boolean require() default false;

    String[] options() default {};
}
