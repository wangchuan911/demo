package my.hehe.webserver.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public abstract class AbstractCustomVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomVerticle.class);

    Router router;
    Set<Handler<Vertx>> VERTX_HANDLERS = null;
    Set<Handler<Router>> ROUTER_HANDLERS = null;


    @SuppressWarnings("unchecked")
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
    }

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
                        Class genericClazz = (Class) (pt2).getActualTypeArguments()[0];
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
            e.printStackTrace();
        }
    }
}


