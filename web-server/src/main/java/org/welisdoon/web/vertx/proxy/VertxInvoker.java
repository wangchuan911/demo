package org.welisdoon.web.vertx.proxy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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

    /*@Override
    public void invoke(String clzName, String methodName, String paramTypesJson, String paramsJson, String threadParamsJson, Handler<AsyncResult<String>> handler) {
        String[] paramTypes = JSONArray.parseArray(paramTypesJson, String.class).toArray(String[]::new);
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
//                    types[i] = Class.forName(paramType);
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
                promise.complete(result != null ? JSONObject.toJSONString(result, SerializerFeature.WriteClassName).toString() : null);
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                logger.error(t.getMessage(), t);
                if (t instanceof NullPointerException) {
                    t = new RuntimeException("空数据异常", t);
                }
                promise.fail(t);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                promise.fail(e);
            }
        });
        f.onComplete(handler);
    }*/

    @Override
    public void check(String clzName, String methodName, String paramTypesJson, String returnType, Handler<AsyncResult<Void>> handler) {
        Future<Void> f = Future.future(promise -> {
            try {
                ClassInfo.MethodInfo methodInfo = getMethodInfo(clzName, methodName, paramTypesJson);
                if (!methodInfo.method.getReturnType().isAssignableFrom(Class.forName(returnType)) || Class.forName(returnType).isAssignableFrom(methodInfo.method.getReturnType())) {
                    throw new NoSuchMethodException(String.format("Method [ %s ] return type is not matched! expect:[ %s ]; actual:[ %s ];", methodName, methodInfo.method.getReturnType().getName(), returnType));
                }
                promise.complete();
            } catch (Throwable e) {
                promise.fail(e);
            }
        });
        f.onComplete(handler);
    }

    /*final static Map<Class, ParamHandler> BASE_CLASS_MAP = new HashMap<>();

    static <T> void ADD(ParamHandler<T> function, Class<T>... classes) {
        for (Class<T> aClass : classes) {
            BASE_CLASS_MAP.put(aClass, function);
        }
    }

    public static ParamHandler GET_BASE_CLASS_HANDLER(Class clz) {
        return BASE_CLASS_MAP.get(clz);
    }

    @FunctionalInterface
    public interface ParamHandler<T> {
        T handler(Object o) throws Throwable;
    }


    static {
        ADD((o) -> TypeUtils.castToDouble(o), double.class, Double.class);
        ADD((o) -> TypeUtils.castToByte(o), byte.class, Byte.class);
        ADD((o) -> TypeUtils.castToShort(o), short.class, Short.class);
        ADD((o) -> TypeUtils.castToInt(o), int.class, Integer.class);
        ADD((o) -> TypeUtils.castToLong(o), long.class, Long.class);
        ADD((o) -> TypeUtils.castToFloat(o), float.class, Float.class);
        ADD((o) -> TypeUtils.castToString(o).charAt(0), char.class, Character.class);
        ADD((o) -> TypeUtils.castToString(o), String.class);
        ADD((o) -> new StringBuilder(TypeUtils.castToString(o)), StringBuilder.class);
        ADD((o) -> new StringBuffer(TypeUtils.castToString(o)), StringBuffer.class);
    }
*/
    class ClassInfo {
        final Class clz;
        final Object target;
        final FastClass fastClass;
        final Map<String, MethodInfo> methodInfos;

        public ClassInfo(String className) throws ClassNotFoundException {
            this.clz = Class.forName(className);
            this.target = ApplicationContextProvider.getBean(this.clz);
            this.fastClass = FastClass.create(clz);
            this.methodInfos = new HashMap<>();
        }

        MethodInfo getMethod(String methodName, String paramTypesJson) throws Throwable {
            return ObjectUtils.getMapValueOrNewSafe(methodInfos, methodName + paramTypesJson, () -> new MethodInfo(methodName, paramTypesJson));
        }

        class MethodInfo {
            final Method method;
            final FastMethod fastMethod;
            final Class[] params;
            final ObjectUtils.ObjectDefineType returnType;

            MethodInfo(String methodName, String paramTypesJson) throws ClassNotFoundException, NoSuchMethodException {
                List<String> pClasses = JSONArray.parseArray(paramTypesJson).toJavaList(String.class);
                this.params = new Class[pClasses.size()];
                for (int i = 0; i < pClasses.size(); i++) {
                    this.params[i] = Class.forName(pClasses.get(i));
                }
                this.method = clz.getMethod(methodName, this.params);
                this.fastMethod = fastClass.getMethod(method);
                this.returnType = ObjectUtils.ObjectDefineType.getInstance(this.method.getGenericReturnType());
            }

            Object invoke(String paramsJson) throws Throwable {
                JSONArray params = JSONArray.parseArray(paramsJson);
                int len = this.params.length;
                Object[] values = new Object[len];
                for (int i = 0; i < len; i++) {
                    values[i] = params.get(i);
                    if (values[i] == null) continue;
                    values[i] = TypeUtils.castToJavaBean(values[i], this.params[i]);
                }
                return this.fastMethod.invoke(target, values);
            }


        }
    }

    final static Map<String, ClassInfo> METHDO_INFO_MAP = new HashMap<>();

    ClassInfo.MethodInfo getMethodInfo(String clzName, String methodName, String paramTypesJson) throws Throwable {
        ClassInfo classInfo = ObjectUtils.getMapValueOrNewSafe(METHDO_INFO_MAP, clzName, () -> new ClassInfo(clzName));
        return classInfo.getMethod(methodName, paramTypesJson);
    }

    @Override
    public void invoke(String clzName, String methodName, String paramTypesJson, String paramsJson, String threadParamsJson, Handler<AsyncResult<String>> handler) {

        Future<String> f = Future.future(promise -> {
            try {

                ClassInfo.MethodInfo methodInfo = getMethodInfo(clzName, methodName, paramTypesJson);
                Object result = methodInfo.invoke(paramsJson);
                if (result == null) {
                    promise.complete(null);
                    return;
                }

                switch (methodInfo.returnType) {
                    case Base:
                        promise.complete(result.toString());
                        break;
                    default:
                        promise.complete(JSONObject.toJSONString(result, SerializerFeature.WriteClassName));
                        break;
                }
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                logger.error(t.getMessage(), t);
                if (t instanceof NullPointerException) {
                    t = new RuntimeException("空数据异常", t);
                }
                promise.fail(t);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                promise.fail(e);
            } finally {

            }
        });
        f.onComplete(handler);
    }
}
