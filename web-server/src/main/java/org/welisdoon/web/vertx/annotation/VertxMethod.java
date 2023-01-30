package org.welisdoon.web.vertx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxMethod {
    String value();

    @interface Caller {
        String targetClassName() default "";

        Class<?> targetClass() default Object.class;

        String value();

        String appId() default "default";
    }
}
