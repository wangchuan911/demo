package org.welisdoon.web.vertx.verticle;

import com.google.common.base.Predicate;
import io.vertx.core.*;

import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.web.Router;


import org.springframework.boot.util.LambdaSafe;
import org.springframework.core.env.Environment;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractMyVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMyVerticle.class);

    private static Entry[] ENTRYS;
    private static volatile boolean prepare = false;

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


    final public synchronized static Future<CompositeFuture> prepare(Vertx vertx, org.welisdoon.web.config.VertxConfiguration options) {
        if (prepare) return Future.failedFuture(new StackOverflowError("verticle is running!"));
        prepare = true;
        Map<String, Entry> map = new HashMap<>();
        Register[] initEntry = options.getRegister().stream().map(aClass -> {
            try {
                return aClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return ApplicationContextProvider.getBean(aClass);
            }
        }).filter(Objects::nonNull).toArray(Register[]::new);

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

        ENTRYS = Arrays
                .stream(initEntry)
                .flatMap(register -> map.entrySet()
                        .stream().map(handlerEntryListEntry -> handlerEntryListEntry.getValue())
                        .filter(entry -> register.getClass().isAssignableFrom(entry.getClass())))
                .toArray(Entry[]::new);

        vertx.registerVerticleFactory(options.getFactory());
        return CompositeFuture
                .all(options.getDeployOptions().entrySet()
                        .stream()
                        .map(entry -> {
                            org.welisdoon.web.vertx.annotation.Verticle verticle = entry.getKey().getAnnotation(org.welisdoon.web.vertx.annotation.Verticle.class);
                            DeploymentOptions deploymentOptions = entry.getValue();
                            String verticleName = String.format("%s:%s", options.getFactory().prefix(), entry.getKey().getName());
                            deploymentOptions.setWorker(verticle.worker());
                            // As worker verticles are never executed concurrently by Vert.x by more than one thread,
                            // deploy multiple instances to avoid serializing requests.

                            Promise<Void> promise = Promise.promise();
                            vertx.deployVerticle(verticleName, deploymentOptions, event -> {
                                promise.complete();
                                if (event.succeeded())
                                    logger.info("deploy success!{}", verticleName);
                                else {
                                    logger.error("Failed to deploy verticle");
                                    logger.error(event.cause().getMessage(), event.cause());
                                }
                            });
                            return promise.future();
                        })
                        .collect(Collectors.toList()))
                .onComplete(event -> {
                    ENTRYS = GCUtils.release(ENTRYS);
                });
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

        default Method[] getMethod(Class<?> aClass, Predicate<? super Method>... predicates) {
            return ReflectionUtils
                    .getAllMethods(aClass, predicates)
                    .stream()
                    .collect(Collectors.toMap(method -> String.format("%s-%s", method.getName(), Arrays.toString(method.getGenericExceptionTypes())), method -> method, (o, o2) -> o2.getDeclaringClass() == aClass ? o2 : o))
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getValue).toArray(Method[]::new);
        }
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

        String key(Object... objects) {
            return this.getClass().getName() + (objects.length > 0 ? "-" : "") + Arrays.stream(objects).map(Object::toString).collect(Collectors.joining("-"));
        }

        abstract void inject(Vertx vertx, AbstractMyVerticle verticle);
    }

    final public static class VertxRegisterEntry extends Entry implements Register {
        Class<? extends AbstractMyVerticle> verticleClass;
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
            /*ReflectionUtils
                    .getAllMethods(aClass, ReflectionUtils.withAnnotation(VertxRegister.class))
                    .stream()
                    .collect(Collectors.toMap(method -> String.format("%s-%s", method.getName(), Arrays.toString(method.getGenericExceptionTypes())), method -> method, (o, o2) -> o2.getDeclaringClass() == aClass ? o2 : o))
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getValue)*/
            Arrays.stream(this.getMethod(aClass, ReflectionUtils.withAnnotation(VertxRegister.class)))
                    .forEach(method -> {
                        VertxRegister annotation = method.getDeclaredAnnotation(VertxRegister.class);
                        try {
                            Class vertCls = annotation.value();
                            Type retunType = (method.getGenericReturnType()), innertype;
                            innertype = (this.isFunctionInterface(retunType)
                                    && retunType instanceof ParameterizedType) ? ((ParameterizedType) retunType).getActualTypeArguments()[0] : void.class;

                            if (vertCls == Verticle.class) return;
                            VertxRegisterEntry vertxRegisterEntry;
                            String key = this.key(vertCls, innertype, aClass);
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
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
        }

        @Override
        public void inject(Vertx vertx, AbstractMyVerticle verticle) {
            Class<? extends AbstractMyVerticle> aClass = verticle.getClass();
            Class<? extends AbstractMyVerticle> verticleClass = this.verticleClass;
            if (verticleClass == aClass) {
                Type VertxRegisterInnerType = this.VertxRegisterInnerType;
                final Object serviceBean = ApplicationContextProvider.getBean(this.ServiceClass);
                Object value = null;
                if (VertxRegisterInnerType == Vertx.class) {
                    value = vertx;
                } else if (VertxRegisterInnerType == Router.class) {
                    value = getRouter(aClass);
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
                        if (this.isFunctionInterface(method.getReturnType())) {
                            method = Arrays.stream(obj.getClass().getMethods())
                                    .filter(method1 -> !method1.isDefault())
                                    .findFirst()
                                    .get();
                            method.setAccessible(true);
                            method.invoke(obj, finalValue);
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                });
            }

        }

        protected boolean isFunctionInterface(Type type) {
            Class<?> clz = ObjectUtils.getTypeClass(type);
            return clz.isInterface() && clz.getAnnotation(FunctionalInterface.class) != null;
        }
    }


}




