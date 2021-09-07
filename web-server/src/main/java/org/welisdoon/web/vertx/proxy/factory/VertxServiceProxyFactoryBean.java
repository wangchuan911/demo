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

import java.lang.annotation.Annotation;
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
public class VertxServiceProxyFactoryBean<T> implements FactoryBean<T>, InvocationHandler {
    static final String REGEXP = "\\{([\\w\\-\\d\\.]+)\\}";
    private Class<T> interfaces;
    private Class<?> tagetClass;
    private VertxServiceProxy serviceProxy;
    T instance;
    IVertxInvoker iVertxInvoker;

    public VertxServiceProxyFactoryBean(Class<T> interfaces) throws ClassNotFoundException {
        this.interfaces = interfaces;
        this.serviceProxy = interfaces.getAnnotation(VertxServiceProxy.class);
        this.tagetClass = this.serviceProxy.targetClass() == null || this.serviceProxy.targetClass() == Object.class ? Class.forName(serviceProxy.targetClassName()) : this.serviceProxy.targetClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        if (this.instance == null) {
            Class[] intfs = interfaces.getSuperclass() != Object.class && interfaces.getSuperclass() != null ?
                    new Class[]{interfaces, interfaces.getSuperclass()} :
                    new Class[]{interfaces};
            this.instance = (T) Proxy.newProxyInstance(interfaces.getClassLoader(), intfs, this);
        }
        return this.instance;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaces;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    synchronized void initInvoker() {
        if (this.iVertxInvoker == null) {
            String address = VertxServiceProxyFactoryBean.this.serviceProxy.address();
            while (address.matches(REGEXP)) {
                address = address.replaceFirst(REGEXP, "$1");
            }
            this.iVertxInvoker = AsyncProxyUtils.
                    createServiceProxyBuilder(
                            ApplicationContextProvider.getBean(StandaredVerticle.class).getVertx(),
                            ApplicationContextProvider.getApplicationContext().getEnvironment().getProperty(address),
                            IVertxInvoker.class);
        }

    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Promise promise = Promise.promise();
        Type retunType = method.getGenericReturnType(),
                innertype = retunType == null ? null : ((ParameterizedType) retunType).getActualTypeArguments()[0];
        if (this.iVertxInvoker == null) {
            this.initInvoker();
        }
        this.iVertxInvoker.invoke(
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
