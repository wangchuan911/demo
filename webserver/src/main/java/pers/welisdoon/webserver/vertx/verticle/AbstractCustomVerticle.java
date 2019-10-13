package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import pers.welisdoon.webserver.WebserverApplication;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    final private static Map<HandlerEntry, Set<Method>> HANDLES = new HashMap<>(4);


    @Value("${vertx.scanPath}")
    private String[] scanPath;


    @Override
    final public void start(Future<Void> startFuture) throws Exception {
        Future future = Future.future();
        future.setHandler(voidAsyncResult -> {
            handleRegist();
            registedAfter(startFuture);
        });
        registedBefore(future);
    }

    @Override
    final public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

    @Override
    final public void start() throws Exception {
        super.start();
    }

    @Override
    final public void stop() throws Exception {
        super.stop();
    }

    abstract void registedBefore(Future future);

    abstract void registedAfter(Future future);

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

    final void scanRegister() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        {
            String[] paths = scanPath;
            Collection<URL> CollectUrl = null;
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                switch (i) {
                    case 0:
                        CollectUrl = ClasspathHelper.forPackage(path);
                        break;
                    default:
                        CollectUrl.addAll(ClasspathHelper.forPackage(path));
                        break;
                }
            }
            CollectUrl = CollectionUtils.isEmpty(CollectUrl) ? ClasspathHelper.forPackage(WebserverApplication.class.getPackageName()) : CollectUrl;
            configurationBuilder.setUrls(CollectUrl);
        }
        Reflections reflections = new Reflections(configurationBuilder);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(VertxConfiguration.class);
        classes.forEach(aClass -> {
            Set<Method> methods = ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(VertxRegister.class));
            methods.forEach(method -> {
                Annotation annotation = method.getDeclaredAnnotation(VertxRegister.class);
                if (annotation == null) return;
                try {
                    Class vertCls = ((VertxRegister) annotation).value();
                    Type retunType = (method.getGenericReturnType());
                    Type innertype = ((ParameterizedType) retunType).getActualTypeArguments()[0];
                    if (vertCls == null || vertCls == Verticle.class || retunType == null) return;
                    synchronized (HANDLES) {
                        {
                            HandlerEntry handlerEntry = new HandlerEntry();
                            handlerEntry.verticleClass = vertCls;
                            handlerEntry.VertxRegisterInnerType = innertype;
                            handlerEntry.ServiceClass = aClass;
                            Set set = HANDLES.get(handlerEntry);
                            if (set == null) {
                                set = new HashSet<>(4);
                                HANDLES.put(handlerEntry, set);
                            }
                            set.add(method);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    final private void handleRegist() {
        Class clazz = this.getClass();
        HANDLES.forEach((handlerEntry, methods) -> {
            Class<? extends AbstractCustomVerticle> verticleClass = handlerEntry.verticleClass;
            if (verticleClass == clazz) {
                Type VertxRegisterInnerType = handlerEntry.VertxRegisterInnerType;
                Class<?> ServiceClass = handlerEntry.ServiceClass;
                Object value = null;
                if (VertxRegisterInnerType == Vertx.class) {
                    value = vertx;
                } else if (VertxRegisterInnerType == Router.class) {
                    value = getSpringBeanFiled(clazz, Router.class).get("router");
                }
                if (value == null) {
                    return;
                }
                final Object finalValue = value;
                methods.forEach(method -> {
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
                                    parameterValue[i] = getSpringBeanFiled(clazz, Router.class).get("router");
                                }
                            }
                        }
                        Object obj = method.invoke(ApplicationContextProvider.getBean(ServiceClass), parameterValue);
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

    @PostConstruct
    final void AbstractCustomVerticleInital() {
        if (CollectionUtils.isEmpty(HANDLES)) {
            synchronized (HANDLES) {
                if (CollectionUtils.isEmpty(HANDLES)) {
                    scanRegister();
                }
            }
        }
    }

    static Map<String, Object> getSpringBeanFiled(Class<?> TargetClass, Class<?> filedClass) {
        Set<Field> fields = ReflectionUtils.getFields(TargetClass, ReflectionUtils.withType(Router.class));
        if (CollectionUtils.isEmpty(fields)) return Map.of();
        Map<String, Object> stringMap = new HashMap<>();
        for (Field field : fields) {
            try {
                stringMap.put(field.getName(), field.get(ApplicationContextProvider.getBean(TargetClass)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stringMap;
    }

    private class HandlerEntry {
        Class<? extends AbstractCustomVerticle> verticleClass;
        Type VertxRegisterInnerType;
        Class<?> ServiceClass;

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




