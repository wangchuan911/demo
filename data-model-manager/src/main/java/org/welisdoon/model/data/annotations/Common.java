package org.welisdoon.model.data.annotations;

import org.welisdoon.model.data.consts.CommonType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Common
 * @Description TODO
 * @Author Septem
 * @Date 9:56
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Common {
    CommonType value();
}
