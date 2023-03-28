package org.welisdoon.model.query.annotations;

import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.query.consts.QueryObjectType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    QueryObjectType value();
}
