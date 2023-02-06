package org.welisdoon.model.data.annotations;

import org.welisdoon.model.data.consts.InputTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {
    InputTypes value();
}
