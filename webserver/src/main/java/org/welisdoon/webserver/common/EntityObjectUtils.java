package org.welisdoon.webserver.common;

import org.apache.commons.collections4.CollectionUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.common.annotation.EntityObjectKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public interface EntityObjectUtils {
    Logger logger = LoggerFactory.getLogger(EntityObjectUtils.class);
    Map<Class<?>, Set<Field>> fieldMap = new HashMap<>();
    Map<Class<?>, Set<Method>> methodMap = new HashMap<>();

    static Object objValueByKey(Object t, String key) {
        Class tKey = t.getClass();
        Set<Field> fields = fieldMap.get(tKey);
        if (CollectionUtils.isEmpty(fields)) {
            fields = ReflectionUtils.getFields(tKey, ReflectionUtils.withAnnotation(EntityObjectKey.class));
            synchronized (fieldMap) {
                if (fieldMap.containsKey(tKey))
                    fieldMap.put(tKey, fields);
            }
        } else {
            fields = fieldMap.get(tKey);
        }
        Optional<Field> optionalField = fields.stream().filter(field -> {
            EntityObjectKey annotation = field.getAnnotation(EntityObjectKey.class);
            return annotation != null && key.equals(annotation.name());
        }).findFirst();

        if (optionalField.isPresent()) {
            try {
                return optionalField.get().get(t);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }

        Set<Method> methods = methodMap.get(tKey);
        if (CollectionUtils.isEmpty(fields)) {
            methods = ReflectionUtils.getMethods(tKey, ReflectionUtils.withAnnotation(EntityObjectKey.class));
            synchronized (methodMap) {
                if (methodMap.containsKey(tKey))
                    methodMap.put(tKey, methods);
            }
        } else {
            methods = methodMap.get(tKey);
        }
        Optional<Method> optionalMethod = methods.stream().filter(field -> {
            EntityObjectKey annotation = field.getAnnotation(EntityObjectKey.class);
            return annotation != null && key.equals(annotation.name());
        }).findFirst();

        if (optionalMethod.isPresent()) {
            try {
                return optionalMethod.get().invoke(t, key);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            Object res = t.getClass().getMethod("get" + key).invoke(t);
//                            logger.warn(res.getClass().getName());
            if (res instanceof Date) {
                res = LocalDateTime.ofInstant(((Date) res).toInstant(), ZoneId.systemDefault());
            } else if (res instanceof Timestamp) {
                res = ((Timestamp) res).toLocalDateTime();
            } else if (res instanceof Calendar) {
                LocalDateTime.ofInstant(((Calendar) res).getTime().toInstant(), ((Calendar) res).getTimeZone().toZoneId());
            }
            if (res instanceof LocalDateTime) {
                res = (String.format("%04d年%02d月%02d日 %02d:%02d",
                        ((LocalDateTime) res).getYear(),
                        ((LocalDateTime) res).getMonth(),
                        ((LocalDateTime) res).getDayOfMonth(),
                        ((LocalDateTime) res).getHour(),
                        ((LocalDateTime) res).getMinute()));
            }
            return res != null ? res.toString() : "";
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static Reflections getReflections() {
        try {
            return ApplicationContextProvider.getBean(Reflections.class);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
