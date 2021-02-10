package org.welisdoon.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.RoutingContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.vertx.annotation.VertxRouter;
import org.welisdoon.webserver.vertx.utils.RoutingContextChain;

import java.lang.ref.SoftReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    private static Entry[] ENTRYS;

    @Override
    final public void start(Promise<Void> startPromise) {
        Promise<Void> promise = Promise.promise();
        promise.future().onComplete(voidAsyncResult -> {
            handleRegist();
            deployAfter(startPromise);
        });
        deployBefore(promise);
    }

    abstract void deployBefore(Promise future);

    abstract void deployAfter(Promise future);

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
        ApplicationContextProvider.getBean(Reflections.class).getTypesAnnotatedWith(VertxConfiguration.class)
                .stream()
                .forEach(aClass -> {
                    Arrays.stream(initEntry).forEach(entry -> {
                        entry.scan(aClass, map);
                    });
                });
        ENTRYS = map.entrySet().stream().map(handlerEntryListEntry -> handlerEntryListEntry.getValue()).toArray(Entry[]::new);
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
        public void scan(Class<?> aClass, Map<String, Entry> map) {
            ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(VertxRegister.class))
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
                final Object serviceBean;
                try {
                    serviceBean = ApplicationContextProvider.getBean(this.ServiceClass);
                } catch (Throwable t) {
                    logger.warn(t.getMessage());
                    return;
                }
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
                this.methods.clear();
                this.methods = null;
            }

        }
    }

    final public static class VertxRouterEntry extends Entry implements Register {
        Method[] routeMethod;
        int fieldsSize;
        static int createLength = 4;
        Set<Class<?>> configBeans;
        private static String REGEX_PATH = "\\{([\\w\\-]+)\\.path\\.(\\w+)\\}(.*)";

        @Override
        public void scan(Class<?> aClass, Map<String, Entry> map) {
            ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(VertxRouter.class))
                    .stream()
                    .filter(method -> Arrays.stream(method.getParameterTypes())
                            .filter(bClass -> bClass == RoutingContextChain.class).findFirst().isPresent())
                    .forEach(method -> {
                        String key = Entry.key(this.getClass(), aClass);
                        VertxRouterEntry routerEntry;
                        if (map.containsKey(key)) {
                            routerEntry = (VertxRouterEntry) map.get(key);
                            int len = routerEntry.fieldsSize;
                            if (len == routerEntry.routeMethod.length) {
                                routerEntry.routeMethod = Arrays.copyOf(routerEntry.routeMethod, len + createLength);
                            }
                            routerEntry.routeMethod[len] = method;
                            routerEntry.fieldsSize++;
                        } else {
                            routerEntry = new VertxRouterEntry();
                            routerEntry.ServiceClass = aClass;
                            routerEntry.routeMethod = new Method[createLength];
                            routerEntry.routeMethod[0] = method;
                            routerEntry.fieldsSize = 1;
                            map.put(key, routerEntry);
                        }
                    });
        }


        @Override
        void inject(Vertx vertx, AbstractCustomVerticle verticle) {
            final Object serviceBean;
            try {
                serviceBean = ApplicationContextProvider.getBean(this.ServiceClass);
            } catch (Throwable t) {
                logger.warn(t.getMessage());
                return;
            }
            if (verticle instanceof AbstractWebVerticle) {
                Arrays.stream(this.routeMethod).filter(Objects::nonNull).forEach(routeMethod -> {
                    VertxRouter vertxRouter = routeMethod.getAnnotation(VertxRouter.class);
                    Route route = ((AbstractWebVerticle) verticle).router.route();
                    RoutingContextChain chain = new RoutingContextChain(route);
                    if (vertxRouter.method() != null && vertxRouter.method().length > 0) {
                        /*Arrays.stream(vertxRouter.method()).forEach(httpMethod -> {
                            route.method(httpMethod);
                        });*/

                        Arrays.stream(vertxRouter.method()).filter(httpMethodString -> {
                                    Optional<HttpMethod> optional = HttpMethod
                                            .values()
                                            .stream()
                                            .filter(httpMethod ->
                                                    httpMethod.name().equals(httpMethodString))
                                            .findFirst();
                                    boolean flag = optional.isPresent();
                                    if (flag) {
                                        route.method(optional.get());
                                    }
                                    return !flag;
                                }
                        ).forEach(s -> {
                            if (StringUtils.isEmpty(s)) return;
                            route.method(new HttpMethod(s));
                        });

                    }
                    String pathString = vertxRouter.path(), part0, part1, part2;
                    final String replaceFormat = "%s$3";
                    if (pathString.matches(REGEX_PATH)) {
                        part0 = pathString.replaceFirst(REGEX_PATH, "$1");
                        Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);
                        if (configBeans == null || configBeans.size() == 0)
                            configBeans = reflections.getTypesAnnotatedWith(ConfigurationProperties.class);
                        Class<AbstractWechatConfiguration> configClass =
                                (Class<AbstractWechatConfiguration>) configBeans.stream().filter(aClass -> {
                                    ConfigurationProperties properties = aClass.getAnnotation(ConfigurationProperties.class);
                                    return AbstractWechatConfiguration.class.isAssignableFrom(aClass)
                                            && (part0.equals(properties.value()) || part0.equals(properties.prefix()));
                                }).findFirst().get();
                        AbstractWechatConfiguration.Path path = AbstractWechatConfiguration.getConfig(configClass).getPath();
                        part1 = pathString.replaceFirst(REGEX_PATH, "$2");
                        switch (part1) {
                            case "app":
                                part2 = path.getApp();
                                break;
                            case "pay":
                                part2 = path.getPay();
                                break;
                            case "push":
                                part2 = path.getPush();
                                break;
                            default:
                                part2 = path.getOther().get(vertxRouter.path());
                        }
                        pathString = (StringUtils.isEmpty(part2)
                                ? vertxRouter.path()
                                : pathString.replaceFirst(REGEX_PATH, String.format(replaceFormat, part2)));
                    }
                    if (vertxRouter.pathRegex()) {
                        route.pathRegex(pathString);
                    } else {
                        route.path(pathString);
                    }
                    route.order(vertxRouter.order());
                    try {
                        Object[] paramaters = Arrays.stream(routeMethod.getParameterTypes()).map(aClass -> {
                            Object value = null;
                            if (aClass == Vertx.class) {
                                value = vertx;
                            } else if (aClass == RoutingContextChain.class) {
                                value = chain;
                            }
                            return value;
                        }).toArray();

                        routeMethod.setAccessible(true);
                        routeMethod.invoke(serviceBean, paramaters);
                        logger.info(String.format("%s[%s]", route.methods(), pathString));
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        logger.error(e.getMessage(), e);
                        return;
                    } finally {
                        SoftReference<RoutingContextChain> reference = new SoftReference<>(chain);
                        configBeans.clear();
                        configBeans = null;
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
                return new Class[]{VertxRegisterEntry.class, VertxRouterEntry.class};
            return register;
        }
    }
}




