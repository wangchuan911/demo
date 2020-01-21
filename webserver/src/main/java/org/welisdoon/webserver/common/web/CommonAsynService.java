package org.welisdoon.webserver.common.web;


import com.github.pagehelper.PageHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.reflections.ReflectionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.JAXBUtils;
import org.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import org.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
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
    @Autowired
    private WeChatService weChatService;

    private static final Logger logger = LoggerFactory.getLogger(CommonAsynService.class);

    final static Map<Class, Class> CLASS_ALIAS_MAP;

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

        int mid = entrys.length / 2;
        for (int i = 0; i < entrys.length; i++) {
            boolean flag = i < mid;
            int a = flag ? i * 2 : (i - mid) * 2 + 1;
            int b = flag ? i * 2 + 1 : (i - mid) * 2;
            entrys[i] = Map.entry(classes[a], classes[b]);
        }
        CLASS_ALIAS_MAP = Map.ofEntries(entrys);
    }

    @Override
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
    }


    @Override
    public void requsetCall(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler) {
        Promise<Response> promise = Promise.promise();
        promise.future().setHandler(outputBodyHandler);
        Response response = new Response();
        JsonObject params = (JsonObject) Json.decodeValue(requset.getParams());
        try {
            Object sprngService = ApplicationContextProvider.getBean(requset.getService());
            Object input = requset.bodyAsJson();
            if (input instanceof JsonArray) {
                List body = ((JsonArray) input).getList();
                Set<Method> methods = ReflectionUtils.getMethods(sprngService.getClass(), method ->
                        method != null && method.isAnnotationPresent(VertxWebApi.class)
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
                        response.setResult(optionalMethod.get().invoke(sprngService, args));
                        return;
                    }
                }
            }
            throw new NoSuchMethodError();
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            response.setException(e.getCause().getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setException(e.getMessage());
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
