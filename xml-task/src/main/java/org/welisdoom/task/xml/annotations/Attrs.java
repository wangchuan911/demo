package org.welisdoom.task.xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Attrs
 * @Description TODO
 * @Author Septem
 * @Date 16:41
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attrs {
    Attr[] value();
}
