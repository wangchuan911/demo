package org.welisdoon.web.vertx.verticle;

import io.vertx.core.*;

import io.vertx.ext.web.Router;


import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    private static Entry[] ENTRYS;

    @Override
    public void start() {
        handleRegist();
    }


    /*public static synchronized <T> void registeredHandler(Class<? extends AbstractCustomVerticle> verticle, Class<T> t, Handler<T> handler) {
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
    }*/


    final public synchronized static void initVertxInSpring(Options options) {
        if (ENTRYS != null) return;
        Map<String, Entry> map = new HashMap<>();
        Register[] initEntry = Arrays.stream(options.getRegister()).map(aClass -> {
            try {
                return aClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return ApplicationContextProvider.getBean(aClass);
            }
        }).filter(Objects::nonNull).toArray(Register[]::new);
        /*ApplicationContextProvider.getBean(Reflections.class).getTypesAnnotatedWith(VertxConfiguration.class)
                .stream()*/
        ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(VertxConfiguration.class)
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(o -> ApplicationContextProvider.getRealClass(o.getClass()))
                .forEach(aClass -> {
                    Arrays.stream(initEntry).forEach(entry -> {
                        entry.scan(aClass, map);
                    });
                });
        ENTRYS = map.entrySet().stream().map(handlerEntryListEntry -> handlerEntryListEntry.getValue()).toArray(Entry[]::new);
        ENTRYS = Arrays.stream(initEntry).flatMap(register -> Arrays.stream(ENTRYS).filter(entry -> register.getClass().isAssignableFrom(entry.getClass()))).toArray(Entry[]::new);
    }


    final private void handleRegist() {
        Arrays.stream(ENTRYS).forEach(entry -> {
            entry.inject(vertx, this);
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

    public static interface Register {
        void scan(Class<?> aClass, Map<String, Entry> map);
    }

    public abstract static class Entry {
        Class<?> ServiceClass;

        @Override
        public int hashCode() {
            return (ServiceClass.toString()).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                Entry entry = (Entry) obj;
                return entry.ServiceClass == this.ServiceClass;
            }
            return false;
        }

        static String key(Object... objects) {
            return Arrays.stream(objects).map(Object::toString).collect(Collectors.joining("-"));
        }

        abstract void inject(Vertx vertx, AbstractCustomVerticle verticle);
    }

    final public static class VertxRegisterEntry extends Entry implements Register {
        Class<? extends AbstractCustomVerticle> verticleClass;
        Type VertxRegisterInnerType;
        Set<Method> methods;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof VertxRegisterEntry) {
                VertxRegisterEntry vertxRegisterEntry = (VertxRegisterEntry) obj;
                return vertxRegisterEntry.verticleClass == this.verticleClass
                        && vertxRegisterEntry.VertxRegisterInnerType == this.VertxRegisterInnerType
                        && vertxRegisterEntry.ServiceClass == this.ServiceClass;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (verticleClass.toString() + VertxRegisterInnerType.toString() + ServiceClass.toString()).hashCode();
        }

        @Override
        public synchronized void scan(Class<?> aClass, Map<String, Entry> map) {
            ReflectionUtils.getAllMethods(aClass, ReflectionUtils.withAnnotation(VertxRegister.class))
                    .stream()
                    .forEach(method -> {
                        VertxRegister annotation = method.getDeclaredAnnotation(VertxRegister.class);
                        try {
                            Class vertCls = annotation.value();
                            Type retunType = (method.getGenericReturnType());
                            if (retunType != null) {
                                if (retunType instanceof ParameterizedType) {
                                    Type innertype = retunType == null ? null : ((ParameterizedType) retunType).getActualTypeArguments()[0];
                                    if (vertCls == null || vertCls == Verticle.class) return;
                                    VertxRegisterEntry vertxRegisterEntry;
                                    String key = Entry.key(this.getClass(), vertCls, innertype, aClass);
                                    if (map.containsKey(key)) {
                                        vertxRegisterEntry = (VertxRegisterEntry) map.get(key);
                                        vertxRegisterEntry.methods.add(method);
                                    } else {
                                        vertxRegisterEntry = new VertxRegisterEntry();
                                        vertxRegisterEntry.verticleClass = vertCls;
                                        vertxRegisterEntry.VertxRegisterInnerType = innertype;
                                        vertxRegisterEntry.ServiceClass = aClass;
                                        vertxRegisterEntry.methods = new HashSet<>();
                                        vertxRegisterEntry.methods.add(method);
                                        map.put(key, vertxRegisterEntry);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        @Override
        public void inject(Vertx vertx, AbstractCustomVerticle verticle) {
            Class<? extends AbstractCustomVerticle> aClass = verticle.getClass();
            Class<? extends AbstractCustomVerticle> verticleClass = this.verticleClass;
            if (verticleClass == aClass) {
                Type VertxRegisterInnerType = this.VertxRegisterInnerType;
                final Object serviceBean = ApplicationContextProvider.getBean(this.ServiceClass);
                Object value = null;
                if (VertxRegisterInnerType == Vertx.class) {
                    value = vertx;
                } else if (VertxRegisterInnerType == Router.class) {
                    value = getRouter(aClass);
                }
                if (value == null) {
                    return;
                }
                final Object finalValue = value;
                this.methods.stream().forEach(method -> {
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
                                    parameterValue[i] = getRouter(aClass);
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

        }
    }


    public static class Options {
        Class<? extends Register>[] register;


        public void setRegister(Class<? extends Register>... register) {
            this.register = register;
        }

        public Class<? extends Register>[] getRegister() {
            if (register == null)
                return new Class[]{VertxRegisterEntry.class, AbstractWebVerticle.VertxRouterEntry.class};
            return register;
        }
    }
}




