package org.welisdoon.webserver.common.web;


import com.github.pagehelper.PageHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.reflections.ReflectionUtils;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.JAXBUtils;
import org.welisdoon.webserver.common.JNIFieldDescriptors;
import org.welisdoon.webserver.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import org.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import org.welisdoon.webserver.service.wechat.service.AbstractWeChatService;
import org.welisdoon.webserver.service.wechat.service.WeChatService;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class CommonAsynService implements ICommonAsynService {

    private static final Logger logger = LoggerFactory.getLogger(CommonAsynService.class);

    final static Map<Class, Class> CLASS_ALIAS_MAP;
    final static Map<Method, MethodProxy> METHOD_PROXY_MAP;

    static {
        Class[] classes = new Class[]{byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class,
                char.class, Character.class,
                JsonArray.class, List.class,
                JsonObject.class, Map.class};
        Map.Entry[] entrys = new Map.Entry[classes.length];

        CLASS_ALIAS_MAP = new HashMap<>(classes.length * 2 + 2, 0.99f);

        int mid = entrys.length / 2;
        for (int i = 0; i < entrys.length; i++) {
            boolean flag = i < mid;
            int a = flag ? i * 2 : (i - mid) * 2 + 1;
            int b = flag ? i * 2 + 1 : (i - mid) * 2;
//            entrys[i] = Map.entry(classes[a], classes[b]);
            CLASS_ALIAS_MAP.put(classes[a], classes[b]);
        }
//        CLASS_ALIAS_MAP = Map.ofEntries(entrys);
        METHOD_PROXY_MAP = new HashMap<>();
    }

    /*@Override
    public void wechatMsgReceive(String message, Handler<AsyncResult<String>> resultHandler) {
        Promise<String> promise = Promise.promise();
        promise.future().setHandler(resultHandler);
        try {
            //报文转对象
            RequestMesseageBody requestMesseageBody = JAXBUtils.fromXML(message, RequestMesseageBody.class);
            //处理数据
            ResponseMesseage responseMesseage = weChatService.receive(requestMesseageBody);
            //报文转对象;
            promise.complete(JAXBUtils.toXML(responseMesseage));
        } catch (Exception e) {
            promise.fail(e);
        }
    }*/


    @Override
    public void callService(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler) {
        Promise<Response> promise = Promise.promise();
        promise.future().onComplete(outputBodyHandler);
        Response response = new Response();
        JsonObject params = StringUtils.isEmpty(requset.getParams()) ? null : (JsonObject) Json.decodeValue(requset.getParams());
        try {
            Object sprngService = ApplicationContextProvider.getBean(requset.getService());
            switch (requset.getMode()) {
                case Requset.WECHAT:
                    RequestMesseageBody requestMesseageBody = JAXBUtils.fromXML(requset.getBody(), RequestMesseageBody.class);
                    //处理数据
                    ResponseMesseage responseMesseage = ((AbstractWeChatService) sprngService).receive(requestMesseageBody);
                    //报文转对象;
                    if (responseMesseage != null)
                        response.setResult(JAXBUtils.toXML(responseMesseage));
                    else
                        promise.fail(MesseageTypeValue.MSG_REPLY);
                    break;
                default:
                    Object input = requset.bodyAsJson();
                    if (input instanceof JsonArray) {
                        List body = ((JsonArray) input).getList();
                        Set<Method> methods = ReflectionUtils.getMethods(sprngService.getClass(), method ->
                                method != null && AnnotationUtils.findAnnotation(method, VertxWebApi.class) != null
                                        && method.getName().equals(requset.getMethod())
                                        && method.getParameterCount() == body.size()
                        );
                        if (!CollectionUtils.isEmpty(methods)) {
                            Object[] args = new Object[body.size()];
                            Optional<Method> optionalMethod = methods.stream()
                                    .filter(method ->
                                            setValueAndCheck(method.getParameterTypes(), args, body)
                                    ).findFirst();
                            if (optionalMethod.isPresent()) {
                                this.setPage(params);
                                if (ApplicationContextProvider.isGCLIBProxy(sprngService.getClass())) {
                                    MethodProxy proxy;
                                    if (METHOD_PROXY_MAP.containsKey(optionalMethod.get())) {
                                        proxy = METHOD_PROXY_MAP.get(optionalMethod.get());
                                    } else {
                                        proxy = MethodProxy.create(
                                                sprngService.getClass(),
                                                sprngService.getClass(),
                                                JNIFieldDescriptors.TO_JNI_STRING(optionalMethod.get()),
                                                requset.getMethod(),
                                                requset.getMethod() + "$$Proxy");
                                        METHOD_PROXY_MAP.put(optionalMethod.get(), proxy);
                                    }
                                    response.setResult(proxy.invoke(sprngService, args));
                                } else {
                                    response.setResult(optionalMethod.get().invoke(sprngService, args));
                                }
                                return;
                            }
                        }
                    }
                    throw new NoSuchMethodError();
            }
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            response.setException(isNullPoiontException(e.getCause()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setException(isNullPoiontException(e.getCause()));
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            response.setError(e.getMessage());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            promise.fail(e);
        } finally {
            if (!promise.future().isComplete()) {
                promise.complete(response);
            }
        }
    }

    private String isNullPoiontException(Throwable throwable) {
        if (throwable instanceof NullPointerException) {
            StackTraceElement s = throwable.getStackTrace()[0];
            return String.format("NULL_DATA:[%s][%d]", s.getClassName(), s.getLineNumber());
        } else if (throwable == null) {
            return "没有数据返回";
        } else {
            return throwable.getMessage();
        }
    }

    boolean setValueAndCheck(Class<?>[] methodParameterTypes, Object[] target, List orgrin) {
        Object value;
        Class valueClass;
        Class paramType;
        for (int i = 0; i < orgrin.size(); i++) {
            value = orgrin.get(i);
            valueClass = value != null ? value.getClass() : Void.class;
            paramType = methodParameterTypes[i];
            if (value != null
                    && !(paramType.isAssignableFrom(valueClass)
                    || (CLASS_ALIAS_MAP.containsKey(paramType)
                    ? CLASS_ALIAS_MAP.get(paramType)
                    : Void.class).isAssignableFrom(valueClass))) {
                return false;
            }
            target[i] = value;
        }
        return true;
    }

    void setPage(JsonObject params) {
        if (params == null || params.size() == 0) return;
        try {
            String val = params.getString("page", null);
            if (!StringUtils.isEmpty(val)) {
                String[] arr = val.split(",");
                PageHelper.startPage(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
}
