package org.welisdoon.web.vertx.proxy.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.Future;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;
import org.welisdoon.web.vertx.verticle.MainWebVerticle;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname VertxInvokerProxyFactoryBean
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 15:43
 */
public class VertxServiceProxyFactoryBean<T> implements FactoryBean<T>, InvocationHandler {
    static final Pattern PATTERN = Pattern.compile("\\{([\\w\\-\\d\\.]+)\\}");
    static final Map<String, IVertxInvoker> iVertxInvokers = new HashMap<>(8, 0.9F);
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

    private String getAddress() {
        /*String address = VertxServiceProxyFactoryBean.this.serviceProxy.address();
        Matcher matcher = PATTERN.matcher(address);
        while (matcher.group()) {

            address = address.replaceFirst(REGEXP, "$1");
        }
        return ApplicationContextProvider.getApplicationContext().getEnvironment().getProperty(address);*/


        StringBuilder address = new StringBuilder(VertxServiceProxyFactoryBean.this.serviceProxy.address());
        Matcher matcher = PATTERN.matcher(VertxServiceProxyFactoryBean.this.serviceProxy.address());
        Deque<int[]> indexs = new LinkedList<>();
        while (matcher.find()) {
            indexs.addFirst(new int[]{matcher.start(), matcher.end()});
        }
        int[] index;
        while ((index = indexs.pollFirst()) != null) {
            address.replace(index[0], index[1], ApplicationContextProvider.getApplicationContext().getEnvironment().getProperty(address.substring(index[0] + 1, index[1] - 1)));
        }
        return address.toString();
    }

    void initInvoker() {
        if (this.iVertxInvoker == null) {
            String key = this.getAddress();
            if (!iVertxInvokers.containsKey(key)) {
                synchronized (iVertxInvokers) {
                    if (!iVertxInvokers.containsKey(key)) {
                        this.iVertxInvoker = AsyncProxyUtils.
                                createServiceProxyBuilder(
                                        ApplicationContextProvider.getBean(MainWebVerticle.class).getVertx(),
                                        key,
                                        IVertxInvoker.class);
                        iVertxInvokers.put(key, this.iVertxInvoker);
                        return;
                    }
                }
            }
            this.iVertxInvoker = iVertxInvokers.get(key);
        }

    }

    /*@Override
    public Future invoke(Object o, Method method, Object[] objects) throws Throwable {
        Promise promise = Promise.promise();
        Type retunType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0],
                innertype = retunType instanceof ParameterizedType ? ((ParameterizedType) retunType).getActualTypeArguments()[0] : null;
        if (this.iVertxInvoker == null) {
            this.initInvoker();
        }
        this.iVertxInvoker.invoke(
                tagetClass.getTypeName(),
                method.getName(),
                JSONArray.toJSONString(
                        Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList())),
                JSONArray.toJSONString(objects),
                "",
                stringAsyncResult -> {
                    if (!stringAsyncResult.succeeded()) {
                        promise.fail(stringAsyncResult.cause());
                        return;
                    }
                    String resultString = stringAsyncResult.result();
                    try {
                        if (resultString == null) {
                            promise.complete(null);
                            return;
                        }
                        if (match(String.class, retunType, innertype)) {
                            promise.complete(resultString);
                            return;
                        }
                        if (this.match(resultString, '{', '}')
                                || (innertype == null && this.match(resultString, '[', ']'))) {
                            promise.complete(JSON.parseObject(stringAsyncResult.result(), retunType));
                            return;
                        }
                        if (this.match(resultString, '[', ']')) {
                            promise.complete(JSON.parseArray(stringAsyncResult.result(), Class.forName(innertype.getTypeName())));
                            return;
                        }
                        promise.complete(resultString);
                    } catch (Throwable e) {
                        promise.fail(e);
                    }
                }
        );
        return promise.future();
    }

    boolean match(String string, char head, char foot) {
        return string.charAt(0) == head
                && string.charAt(string.length() - 1) == foot;
    }

    boolean match(Type a, Type... b) {
        return b.length == 0 ? false : Arrays.stream(b).anyMatch(aClass -> aClass == a);
    }*/

    static class MethodInfo {
        final Class[] paramTypes;
        final String paramTypesStr;
        final ObjectUtils.ObjectDefineInfo returnType;
        final Class returnClass;

        MethodInfo(Method method) throws ClassNotFoundException {
            this.returnType = new ObjectUtils.ObjectDefineInfo(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
            this.paramTypes = method.getParameterTypes();
            this.paramTypesStr = JSONArray.toJSONString(Arrays.stream(this.paramTypes).map(Class::getName).toArray(String[]::new));
            this.returnClass = Class.forName(this.returnType.getName());
        }
    }

    final static Map<Method, MethodInfo> METHDO_INFO_MAP = new HashMap<>();

    @Override

    public Future invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (this.iVertxInvoker == null) {
            this.initInvoker();
        }
        MethodInfo methdoInfo = ObjectUtils.getMapValueOrNewSafe(METHDO_INFO_MAP, method, () -> new MethodInfo(method));
        return this.iVertxInvoker.invoke(
                tagetClass.getTypeName(),
                method.getName(),
                methdoInfo.paramTypesStr,
                JSONArray.toJSONString(objects, SerializerFeature.WriteClassName),
                "")
                .compose(resultString -> {
                    try {
                        switch (methdoInfo.returnType.getType()) {
                            case Base:
                                return Future.succeededFuture(TypeUtils.castToJavaBean(resultString, methdoInfo.returnClass));
                            default:
                                return Future.succeededFuture(this.toJavaObject(JSON.parse(resultString), methdoInfo.returnClass));
                        }
                    } catch (Throwable e) {
                        return Future.failedFuture(e);
                    } finally {

                    }
                });
    }

    Object toJavaObject(Object object, Class<?> tagetClass) {
        if (object instanceof JSON)
            return JSON.toJavaObject((JSON) object, tagetClass);
        return object;
    }
}
