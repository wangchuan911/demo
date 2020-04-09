package org.welisdoon.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;

import org.springframework.util.CollectionUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    private static HandlerEntry[] HANDLES;


    @Override
    final public void start(Promise<Void> startPromise) {
        Promise<Void> promise = Promise.promise();
        promise.future().setHandler(voidAsyncResult -> {
            handleRegist();
            registedAfter(startPromise);
        });
        registedBefore(promise);
    }

    abstract void registedBefore(Promise future);

    abstract void registedAfter(Promise future);

    public static synchronized <T> void registeredHandler(Class<? extends AbstractCustomVerticle> verticle, Class<T> t, Handler<T> handler) {
        try {
            Field[] fields = verticle.getDeclaredFields();
            for (int i = 0; fields != null && i < fields.length; i++) {
                Field field = fields[i];
                if (field.getType() == java.util.Set.class) {
                    Type genericType = field.getGenericType();
                    if (genericType == null) continue;
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        //得到泛型里的class类型对象
                        ParameterizedType pt2 = (ParameterizedType) (pt.getActualTypeArguments()[0]);
                        if (pt2.getRawType() == io.vertx.core.Handler.class
                                && pt2.getActualTypeArguments()[0] == t
                                && Modifier.isStatic(field.getModifiers())) {
                            java.util.Set set = (java.util.Set) field.get(null);
                            if (set == null) {
                                field.set(null, set = new ConcurrentHashSet<T>());
                            }
                            set.add(handler);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("注册执行器失败", e);
        }
    }

    final public synchronized static void scanRegister(Reflections reflections) {
        if (HANDLES != null) return;
        Map<HandlerEntry, List<Method>> map = new HashMap<>();
        reflections.getTypesAnnotatedWith(VertxConfiguration.class)
                .stream()
                .forEach(aClass -> {
                    ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(VertxRegister.class))
                            .stream()
                            .forEach(method -> {
                                VertxRegister annotation = method.getDeclaredAnnotation(VertxRegister.class);
                                try {
                                    Class vertCls = annotation.value();
                                    Type retunType = (method.getGenericReturnType());
                                    Type innertype = retunType == null ? null : ((ParameterizedType) retunType).getActualTypeArguments()[0];
                                    if (vertCls == null || vertCls == Verticle.class || retunType == null) return;

                                    HandlerEntry handlerEntry = new HandlerEntry();
                                    handlerEntry.verticleClass = vertCls;
                                    handlerEntry.VertxRegisterInnerType = innertype;
                                    handlerEntry.ServiceClass = aClass;
                                    synchronized (map) {
                                        List<Method> list = map.get(handlerEntry);
                                        if (CollectionUtils.isEmpty(list)) {
                                            list = new LinkedList<>();
                                            map.put(handlerEntry, list);
                                        }
                                        list.add(method);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                });
        HANDLES = map.entrySet().stream().map(handlerEntryListEntry -> {
            HandlerEntry entry = handlerEntryListEntry.getKey();
            entry.methods = handlerEntryListEntry.getValue().stream().toArray(Method[]::new);
            return entry;
        }).toArray(HandlerEntry[]::new);
    }

    final private void handleRegist() {
        Class clazz = this.getClass();
        Arrays.stream(HANDLES).forEach(handlerEntry -> {
            Class<? extends AbstractCustomVerticle> verticleClass = handlerEntry.verticleClass;
            if (verticleClass == clazz) {
                Type VertxRegisterInnerType = handlerEntry.VertxRegisterInnerType;
                final Object serviceBean;
                try {
                    serviceBean = ApplicationContextProvider.getBean(handlerEntry.ServiceClass);
                } catch (Throwable t) {
                    logger.warn(t.getMessage());
                    return;
                }
                Object value = null;
                if (VertxRegisterInnerType == Vertx.class) {
                    value = vertx;
                } else if (VertxRegisterInnerType == Router.class) {
                    value = getRouter(clazz);
                }
                if (value == null) {
                    return;
                }
                final Object finalValue = value;
                Arrays.stream(handlerEntry.methods).forEach(method -> {
                    try {
                        Class<?>[] parameterTypesClasses = method.getParameterTypes();
                        Object[] parameterValue = null;
                        if (parameterTypesClasses != null && parameterTypesClasses.length > 0) {
                            parameterValue = new Object[parameterTypesClasses.length];
                            for (int i = 0; i < parameterTypesClasses.length; i++) {
                                Class parameterTypeClass = parameterTypesClasses[i];
                                if (parameterTypeClass == Vertx.class) {
                                    parameterValue[i] = vertx;
                                } else if (parameterTypeClass == Router.class) {
                                    parameterValue[i] = getRouter(clazz);
                                }
                            }
                        }
                        Object obj = method.invoke(serviceBean, parameterValue);
                        if (obj instanceof Consumer) {
                            ((Consumer) obj).accept(finalValue);
                        } else if (obj instanceof Handler) {
                            ((Handler) obj).handle(finalValue);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    static <T> T getFiled(Class<?> TargetClass, Class<T> filedClass) {
        Set<Field> fields = ReflectionUtils.getFields(TargetClass, ReflectionUtils.withType(filedClass));
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                return (T) field.get(ApplicationContextProvider.getBean(TargetClass));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static Router getRouter(Class<?> TargetClass) {
        if (TargetClass == AbstractWebVerticle.class) {
            return getFiled(TargetClass, Router.class);
        } else if ((TargetClass = TargetClass.getSuperclass()) != null) {
            return getRouter(TargetClass);
        }
        return null;
    }

    private static class HandlerEntry {
        Class<? extends AbstractCustomVerticle> verticleClass;
        Type VertxRegisterInnerType;
        Class<?> ServiceClass;
        Method[] methods;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HandlerEntry) {
                HandlerEntry handlerEntry = (HandlerEntry) obj;
                return handlerEntry.verticleClass == this.verticleClass
                        && handlerEntry.VertxRegisterInnerType == this.VertxRegisterInnerType
                        && handlerEntry.ServiceClass == this.ServiceClass;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (verticleClass.toString() + VertxRegisterInnerType.toString() + ServiceClass.toString()).hashCode();
        }
    }
}




