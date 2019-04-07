package pers.welisdoon.webserver.vertx.verticle;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;
import pers.welisdoon.webserver.WebserverApplication;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import pers.welisdoon.webserver.WebserverApplication;
import pers.welisdoon.webserver.common.ApplicationContextProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    /*Router router;
    Set<Handler<Vertx>> VERTX_HANDLERS = null;
    Set<Handler<Router>> ROUTER_HANDLERS = null;*/
    static Map<Class<? extends AbstractCustomVerticle>, Map<Type, Set<?>>> HANDLES = new HashMap<>(4);

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

    /*@SuppressWarnings("unchecked")
    public synchronized <T> void registeredHandler(Class<T> t, Handler<T> handler) {
        if (Vertx.class.getName().equals(t.getName())) {
            if (VERTX_HANDLERS == null) {
                VERTX_HANDLERS = new ConcurrentHashSet<>(4);
            }
            if (handler == null) return;
            VERTX_HANDLERS.add((Handler<Vertx>) handler);
        } else if (Router.class.getName().equals(t.getName())) {
            if (ROUTER_HANDLERS == null) {
                ROUTER_HANDLERS = new ConcurrentHashSet<>(4);
            }
            if (handler == null) return;
            ROUTER_HANDLERS.add((Handler<Router>) handler);
        } else {
            throw new RuntimeException("Error Object Type!");
        }
        {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            logger.info("registered Handler Router->" + stackTraceElement.getMethodName() + "[" + stackTraceElement.getLineNumber() + "]");
        }
    }

    void startRegister() {
        if (!CollectionUtils.isEmpty(VERTX_HANDLERS))
            for (Handler<Vertx> vertxHandler : VERTX_HANDLERS) {
                vertxHandler.handle(vertx);
            }
        if (router != null && !CollectionUtils.isEmpty(ROUTER_HANDLERS))
            for (Handler<Router> routerHandler : ROUTER_HANDLERS) {
                routerHandler.handle(router);
            }
    }*/


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

    static void scanRegister() {
        Reflections reflections = new Reflections(WebserverApplication.class.getPackageName());
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
                    Object retrunObJ = method.invoke(ApplicationContextProvider.getBean(aClass), null);
                    synchronized (HANDLES) {
                        Map<Type, Set<?>> map = HANDLES.get(vertCls);
                        if (map == null) {
                            map = new HashMap<>(4);
                            HANDLES.put(vertCls, map);
                        }
                        Set set = map.get(innertype);
                        if (set == null) {
                            set = new HashSet<>(4);
                            map.put(innertype, set);
                        }
                        set.add(retrunObJ);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void handleRegist() {
        Class clazz = this.getClass();
        HANDLES.forEach((aClass, typeSetMap) -> {
            if (aClass == clazz) {
                typeSetMap.forEach((type, objects) -> {
                    Object value = null;
                    if (type == Vertx.class) {
                        value = vertx;
                    } else if (type == Router.class) {
                        Set<Field> fields = ReflectionUtils.getFields(clazz, ReflectionUtils.withType(Router.class));
                        for (Field field : fields) {
                            try {
                                value = field.get(ApplicationContextProvider.getBean(clazz));
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (value == null) {
                        return;
                    }
                    for (Object obj : objects) {
                        if (obj instanceof Consumer) {
                            ((Consumer) obj).accept(value);
                        } else if (obj instanceof Handler) {
                            ((Handler) obj).handle(value);
                        }
                    }
                });
            }
        });
    }

    static {
        scanRegister();
    }
}



