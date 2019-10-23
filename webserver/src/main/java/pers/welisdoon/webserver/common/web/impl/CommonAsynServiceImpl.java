package pers.welisdoon.webserver.common.web.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.common.JAXBUtils;
import pers.welisdoon.webserver.common.web.Requset;
import pers.welisdoon.webserver.common.web.Response;
import pers.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import pers.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import pers.welisdoon.webserver.service.wechat.service.WeChatService;
import pers.welisdoon.webserver.common.web.CommonAsynService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Component
public class CommonAsynServiceImpl implements CommonAsynService {
    @Autowired
    private WeChatService weChatService;

    private static final Logger logger = LoggerFactory.getLogger(CommonAsynServiceImpl.class);

    @Override
    public void wechatMsgReceive(String message, Handler<AsyncResult<String>> resultHandler) {
        Future<String> future = Future.future();
        try {
            //报文转对象
            RequestMesseageBody requestMesseageBody = JAXBUtils.fromXML(message, RequestMesseageBody.class);
            //处理数据
            ResponseMesseage responseMesseage = weChatService.receive(requestMesseageBody);
            //报文转对象;
            future.setHandler(resultHandler);
            future.complete(JAXBUtils.toXML(responseMesseage));
        }
        catch (Exception e) {
            future = Future.future();
            future.fail(e);
        }
    }

    @Override
    public void serviceCall(String serverName, String method, String input, String option, Handler<AsyncResult<String>> outputBodyHandler) {
        Future<String> future = Future.future();
        future.setHandler(outputBodyHandler);
        JsonObject jsonObject = new JsonObject();
        try {
            Object sprngService = ApplicationContextProvider.getBean(serverName);
            Method[] methods = sprngService.getClass().getMethods();
            JsonArray arg = new JsonArray(input);
            Object reult = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(method) && methods[i].getParameterCount() == arg.size()) {
                    Object[] args = new Object[arg.size()];
                    Class<?>[] classes = methods[i].getParameterTypes();
                    /*boolean isThisMethod = true;
                    for (int j = 0; j < arg.size(); j++) {
                        Object value = arg.getValue(j);
                        if (value == null) {
                            isThisMethod = true;
                            classes[j] = typeChange(classes[j]);
                        }
                        else {
                            if (!(isThisMethod = ((classes[j] = classEquals(classes[j], value.getClass(), false)) != null)))
                                break;
                        }
                        args[j] = getValue(value);
                    }*/
                    boolean isThisMethod = checkArgs(args, classes, arg);
                    if (isThisMethod) {
                        reult = methods[i].invoke(sprngService, args);
                        jsonObject.put("result", reult);
                        future.complete(jsonObject.toString());
                        return;
                    }
                }
            }
            throw new NoSuchMethodError();
        }
        catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("exception", e.getMessage());
        }
        catch (Error e) {
            e.printStackTrace();
            jsonObject.put("error", e.getMessage());
        }
        catch (Throwable e) {
            e.printStackTrace();
            future.fail(e);
        }
        finally {
            if (!future.isComplete()) {
                future.complete(jsonObject.toString());
            }
        }
    }

    @Override
    public void requsetCall(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler) {
        Future<Response> future = Future.future();
        future.setHandler(outputBodyHandler);
        Response response = new Response();
        try {
            Object sprngService = ApplicationContextProvider.getBean(requset.getService());
            Method[] methods = sprngService.getClass().getMethods();
            Object body = requset.getBody();
            if (body instanceof JsonArray) {
                JsonArray arg = (JsonArray) requset.getBody();
                Object reult = null;
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(requset.getMethod()) && methods[i].getParameterCount() == arg.size()) {
                        Object[] args = new Object[arg.size()];
                        Class<?>[] classes = methods[i].getParameterTypes();
                        boolean isThisMethod = checkArgs(args, classes, arg);
                        if (isThisMethod) {
                            reult = methods[i].invoke(sprngService, args);
                            response.setResult(reult);
                            future.complete(response);
                            return;
                        }
                    }
                }
            }
            else {
                JsonObject arg = (JsonObject) body;
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(requset.getMethod()) && methods[i].getParameterCount() == 1) {
                        Object o = null;
                        try {
                            o = arg.mapTo(methods[i].getParameters()[0].getClass());
                            o = methods[i].invoke(sprngService, o);
                            response.setResult(o);
                            future.complete(response);
                            return;
                        }
                        catch (Throwable e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                }
            }

            throw new NoSuchMethodError();
        }
        catch (Exception e) {
            e.printStackTrace();
            response.setException(e.getMessage());
        }
        catch (Error e) {
            e.printStackTrace();
            response.setError(e.getMessage());
        }
        catch (Throwable e) {
            e.printStackTrace();
            future.fail(e);
        }
        finally {
            if (!future.isComplete()) {
                future.complete(response);
            }
        }
    }

    private boolean checkArgs(Object[] args, Class<?>[] classes, JsonArray arg) {
        boolean isThisMethod = true;
        for (int j = 0; j < arg.size(); j++) {
            Object value = arg.getValue(j);
            if (value == null) {
                isThisMethod = true;
                classes[j] = typeChange(classes[j]);
            }
            else {
                if (!(isThisMethod = ((classes[j] = classEquals(classes[j], value.getClass(), false)) != null)))
                    break;
            }
            args[j] = getValue(value);
        }
        return isThisMethod;
    }

    private static Class<?> classEquals(Class<?> a, Class<?> b, boolean returnObjClass) {
        if (a == b) {
            return a;
        }
        else if (typeChange(a) == b) {
            return returnObjClass ? b : a;
        }
        else if (a == typeChange(b)) {
            return returnObjClass ? b : a;
        }
        else {
            return null;
        }
    }

    private static Class<?> classEquals(Class<?> a, Class<?> b) {
        return classEquals(a, b, false);
    }

    private static Class<?> typeChange(Class<?> a) {
        if (a == int.class) {
            return Integer.class;
        }
        else if (a == char.class) {
            return Character.class;
        }
        else if (a == byte.class) {
            return Byte.class;
        }
        else if (a == long.class) {
            return Long.class;
        }
        else if (a == float.class) {
            return Float.class;
        }
        else if (a == double.class) {
            return Double.class;
        }
        else if (a == boolean.class) {
            return Boolean.class;
        }
        else if (a == JsonArray.class) {
            return List.class;
        }
        else if (a == JsonObject.class) {
            return Map.class;
        }
        else {
            return a;
        }
    }

    private static Object getValue(Object targetValue) {
        if (targetValue instanceof JsonObject) {
            return ((JsonObject) targetValue).getMap();
        }
        else if (targetValue instanceof JsonArray) {
            return ((JsonArray) targetValue).getList();
        }
        else {
            return targetValue;
        }
    }
}
