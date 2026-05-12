package org.welisdoon.common.object.wrapper.execute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname ExecuteWarpper
 * @Description TODO
 * @Author Septem
 * @Date 9:46
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteWrapper {
    Class<? extends BaseProxyProcessor> value() default BaseProxyProcessor.class;
}
