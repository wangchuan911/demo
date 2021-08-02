package org.welisdoon.web.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityObjectKey {
    String name();

    Class<? extends EntitySpecialType> specialType() default EntitySpecialType.class;

}
