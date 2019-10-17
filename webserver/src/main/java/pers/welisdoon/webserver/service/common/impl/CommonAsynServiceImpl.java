package pers.welisdoon.webserver.service.common.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.common.JAXBUtils;
import pers.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import pers.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import pers.welisdoon.webserver.service.WeChatService;
import pers.welisdoon.webserver.service.common.CommonAsynService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

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
        } catch (Exception e) {
            future = Future.future();
            future.fail(e);
        }
    }

    @Override
    public void servceCall(String serverName, String method, String input, Handler<AsyncResult<String>> outputBodyHandler) {
        Future<String> future = Future.future();
        JsonObject jsonObject = new JsonObject();
        try {
            Object sprngService = ApplicationContextProvider.getBean(serverName);
            Method[] methods = sprngService.getClass().getMethods();
            JsonArray arg = new JsonArray(input);
            Object reult = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getParameterCount() == arg.size()) {
                    Object[] args = new Object[arg.size()];
                    Class<?>[] classes = methods[i].getParameterTypes();
                    boolean isThisMethod = true;
                    for (int j = 0; j < arg.size(); j++) {
                        Object value = arg.getValue(j);
                        if (value == null) {
                            isThisMethod = true;
                            classes[j] = typeChange(classes[j]);
                        } else {
                            if (!(isThisMethod = ((classes[j] = classEquals(classes[j], value.getClass(), false)) != null)))
                                break;
                        }
                        args[j] = value;
                    }
                    if (isThisMethod) {
                        reult = methods[i].invoke(sprngService, args);
                        break;
                    }
                }
            }
            jsonObject.put("result", reult);
        } catch (Throwable e) {
            e.printStackTrace();
            jsonObject.put("error", e.getMessage());
        }
        future.setHandler(outputBodyHandler);
        future.complete(jsonObject.toString());
    }

    private static Class<?> classEquals(Class<?> a, Class<?> b, boolean returnObjClass) {
        if (a == b) {
            return a;
        } else if (typeChange(a) == b) {
            return returnObjClass ? b : a;
        } else {
            return null;
        }
    }

    private static Class<?> classEquals(Class<?> a, Class<?> b) {
        return classEquals(a, b, false);
    }

    private static Class<?> typeChange(Class<?> a) {
        if (a == int.class) {
            return Integer.class;
        } else if (a == char.class) {
            return Character.class;
        } else if (a == byte.class) {
            return Byte.class;
        } else if (a == long.class) {
            return Long.class;
        } else if (a == float.class) {
            return Float.class;
        } else if (a == double.class) {
            return Double.class;
        } else if (a == boolean.class) {
            return Boolean.class;
        } else {
            return a;
        }
    }
}
