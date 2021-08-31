package org.welisdoon.web.vertx.proxy;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VertxInvoker implements IVertxInvoker {

    private static final Logger logger = LoggerFactory.getLogger(VertxInvoker.class);

    /*final static Map<Class, Class> CLASS_ALIAS_MAP;

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
    }*/

    @Override
    public void invoke(String clzName, String methodName, String paramTypesJson, String paramsJson, Handler<AsyncResult<String>> handler) {
        String[] paramTypes = JSONArray.parseArray(paramTypesJson, String.class).stream().toArray(String[]::new);
        JSONArray params = JSONArray.parseArray(paramsJson);
        Future<String> f = Future.future(promise -> {
            try {
                Object value;
                int len = paramTypes.length, index;
                Class<?> clazz = Class.forName(clzName);
                Class<?>[] types = new Class[len];
                Object[] values = new Object[len];
                String paramType, valueString;
                String[] subTypes;
                boolean baseType = false;
                for (int i = 0; i < len; i++) {
                    paramType = paramTypes[i];
                    index = paramType.indexOf("<");
                    if (index > 0) {
                        subTypes = paramType.substring(index + 1, paramType.indexOf(">")).split(",");
                        paramType = paramType.substring(0, index);
                    } else {
                        subTypes = null;
                    }
                    types[i] = Class.forName(paramType);
                    valueString = params.getString(i);
                    if (StringUtils.isEmpty(valueString)) continue;
                    switch (paramType) {
                        case "byte":
                            baseType = true;
                        case "java.lang.Byte":
                            value = params.getByte(i);
                            break;
                        case "short":
                            baseType = true;
                        case "java.lang.Short":
                            value = params.getShort(i);
                            break;
                        case "int":
                            baseType = true;
                        case "java.lang.Integer":
                            value = params.getInteger(i);
                            break;
                        case "long":
                            baseType = true;
                        case "java.lang.Long":
                            value = params.getLong(i);
                            break;
                        case "float":
                            baseType = true;
                        case "java.lang.Float":
                            value = params.getFloat(i);
                            break;
                        case "double":
                            baseType = true;
                        case "java.lang.Double":
                            value = params.getDouble(i);
                            break;
                        case "char":
                            baseType = true;
                        case "java.lang.Character":
                            value = Character.valueOf(params.getString(i).charAt(0));
                            break;
                        case "java.util.List":
                            value = JSONArray.parseArray(params.getString(i)).toJavaList(Class.forName(subTypes.length > 0 ? subTypes[0] : Object.class.getName()));
                            break;
                        case "java.util.Map":
                            value = JSONObject.parseObject(params.getString(i)).toJavaObject(Map.class);
                            break;
                        case "java.lang.String":
                            value = params.getString(i);
                            break;
                        default:
                            value = params.getJSONObject(i).toJavaObject(Class.forName(paramType));

                    }
                    types[i] = Class.forName(paramType);
                    values[i] = value;
                }
                Object result = clazz.getMethod(methodName, types).invoke(ApplicationContextProvider.getBean(clazz), values);
                promise.complete(result != null ? JSONObject.toJSON(result).toString() : null);
            } catch (InvocationTargetException e) {
                logger.error(e.getMessage(), e);
                promise.fail(e.getCause());
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                promise.fail(e);
            }
        });
        f.onComplete(handler);
    }


}
