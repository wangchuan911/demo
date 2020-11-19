package org.welisdoon.webserver.common;

import org.apache.commons.collections4.CollectionUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.common.annotation.EntityObjectKey;
import org.welisdoon.webserver.common.annotation.EntitySpecialType;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public interface EntityObjectUtils {
    Logger logger = LoggerFactory.getLogger(EntityObjectUtils.class);
    Map<Class<?>, EntityObjectDeclare> CLASS_SET_MAP = new HashMap<>();

    static Object objValueByKey(Object t, String key) {
        Class tKey = t.getClass();
        //----------------------------------

        if (!CLASS_SET_MAP.containsKey(tKey)) {
            synchronized (CLASS_SET_MAP) {
                if (!CLASS_SET_MAP.containsKey(tKey)) {
                    CLASS_SET_MAP.put(tKey, new EntityObjectDeclare());
                }
            }
        }
        EntityObjectDeclare entityObjectDeclare = CLASS_SET_MAP.get(tKey);

        if (entityObjectDeclare.annotatoionAccessibleObjects == null) {
            synchronized (CLASS_SET_MAP) {
                if (entityObjectDeclare.annotatoionAccessibleObjects == null) {
                    entityObjectDeclare.annotatoionAccessibleObjects = Arrays
                            .stream(
                                    new AccessibleObject[][]{
                                            ReflectionUtils.getFields(tKey, ReflectionUtils.withAnnotation(EntityObjectKey.class)).stream().toArray(AccessibleObject[]::new)
                                            , ReflectionUtils.getMethods(tKey, ReflectionUtils.withAnnotation(EntityObjectKey.class)).stream().toArray(AccessibleObject[]::new)}
                            )
                            .flatMap(set -> Arrays.stream(set))
                            .toArray(AccessibleObject[]::new);
                }
            }
        }
        AccessibleObject[] accessibleObjects = entityObjectDeclare.annotatoionAccessibleObjects;

        Optional<AccessibleObject> optional = Arrays
                .stream(accessibleObjects)
                .filter(accessibleObject -> key
                        .equals(accessibleObject.getAnnotation(EntityObjectKey.class).name()))
                .findFirst();

        if (optional.isPresent()) {
            AccessibleObject val = optional.get();
            try {
                return getValue(val, t);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (entityObjectDeclare.getMethod == null) {
            synchronized (CLASS_SET_MAP) {
                if (entityObjectDeclare.getMethod == null) {
                    entityObjectDeclare.getMethod = Arrays.stream(tKey.getMethods()).filter(method ->
                            method.getName().indexOf("get") == 0 && method.getParameterCount() == 0
                    ).toArray(Method[]::new);
                }
            }
        }
        Method[] getMethods = entityObjectDeclare.getMethod;
        try {
            char first = key.charAt(0);
            String mthName = "get" + ((first >= 'a' && first <= 'z') ? (char) (first - 32) : first) + key.substring(1);
            Optional<Method> optionalMethod = Arrays.stream(getMethods).filter(method -> method.getName().equals(mthName)).findFirst();
            if (optionalMethod.isEmpty()) return null;
            Object res = optionalMethod.get().invoke(t);
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
                        ((LocalDateTime) res).getMonth().getValue(),
                        ((LocalDateTime) res).getDayOfMonth(),
                        ((LocalDateTime) res).getHour(),
                        ((LocalDateTime) res).getMinute()));
            }
            return res != null ? res.toString() : "";
        } catch (InvocationTargetException | IllegalAccessException e) {
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

    private static Object getValue(AccessibleObject val, Object target) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        EntityObjectKey entityObjectKey;
        Object value;
        if (val instanceof Method) {
            Method method = ((Method) val);
            entityObjectKey = val.getAnnotation(EntityObjectKey.class);
            value = method.invoke(target);
        } else {
            Field field = ((Field) val);
            entityObjectKey = field.getAnnotation(EntityObjectKey.class);
            value = field.get(target);
        }
        if (entityObjectKey != null) {
            Class<? extends EntitySpecialType> specialTypeClass = entityObjectKey.specialType();
            if (specialTypeClass.equals(EntitySpecialType.class)) {
                return value;
            } else {
                EntitySpecialType specialType = ApplicationContextProvider.getBean(specialTypeClass);
                return specialType.getValue(value);
            }
        }
        return value;
    }
}

class EntityObjectDeclare {
    AccessibleObject[] annotatoionAccessibleObjects;
    Method[] getMethod;
}
