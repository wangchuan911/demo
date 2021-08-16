package org.welisdoon.web.vertx.proxy.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Classname VertxInvokerProxyFactoryBean
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 15:43
 */
public class VertxServiceProxyFactoryBean<T> implements FactoryBean<T> {
    private Class<T> interfaces;
    IVertxInvoker iVertxInvoker;

    public VertxServiceProxyFactoryBean(Class<T> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        Class[] intfs = interfaces.getSuperclass() != Object.class && interfaces.getSuperclass() != null ?
                new Class[]{interfaces, interfaces.getSuperclass()} :
                new Class[]{interfaces};
        return (T) Proxy.newProxyInstance(interfaces.getClassLoader(), intfs, new ServiceProxyInvocationHandler());
    }

    @Override
    public Class<?> getObjectType() {
        return interfaces;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    class ServiceProxyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            Type tagetClass = interfaces.getAnnotation(VertxServiceProxy.class).targetClass();
            if (tagetClass == null || tagetClass == Object.class) {
                tagetClass = Class.forName(interfaces.getAnnotation(VertxServiceProxy.class).targetClassName());
            }
            Promise promise = Promise.promise();

            Type retunType = method.getGenericReturnType(),
                    innertype = retunType == null ? null : ((ParameterizedType) retunType).getActualTypeArguments()[0];
            if (iVertxInvoker == null) {
                synchronized (this) {
                    if (iVertxInvoker == null)
                        iVertxInvoker = AsyncProxyUtils.createServiceBinder(ApplicationContextProvider.getBean(StandaredVerticle.class).getVertx(), "common", IVertxInvoker.class);
                }
            }
            iVertxInvoker.invoke(
                    tagetClass.getTypeName(),
                    method.getName(),
                    JSONArray.toJSONString(
                            Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList())),
                    JSONArray.toJSONString(objects),
                    stringAsyncResult -> {
                        if (stringAsyncResult.succeeded()) {
                            String resultJson = stringAsyncResult.result();
                            if (resultJson == null) {
                                promise.complete(null);
                            } else if ("java.lang.String".equals(innertype.getTypeName())) {
                                promise.complete(resultJson);
                            } else if (resultJson.indexOf("[") == 0
                                    && resultJson.lastIndexOf("]") == resultJson.length() - 1) {
                                try {
                                    promise.complete(JSON.parseArray(stringAsyncResult.result(), Class.forName(innertype.getTypeName())));
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else if (resultJson.indexOf("{") == 0
                                    && resultJson.lastIndexOf("}") == resultJson.length() - 1) {
                                try {
                                    promise.complete(JSON.parseObject(stringAsyncResult.result(), Class.forName(innertype.getTypeName())));
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                promise.complete(resultJson);
                            }

                        } else {
                            promise.fail(stringAsyncResult.cause());
                        }
                    }
            );
            return promise.future();
        }
    }
}
