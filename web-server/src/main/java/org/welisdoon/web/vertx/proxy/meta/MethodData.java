package org.welisdoon.web.vertx.proxy.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.welisdoon.common.object.wrapper.execute.BaseProxyProcessor;
import org.welisdoon.common.object.wrapper.execute.ExecuteWrapperProcessor;
import org.welisdoon.common.object.wrapper.execute.Executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Classname InvokerMetaData
 * @Description TODO
 * @Author Septem
 * @Date 20:07
 */
@DataObject
public class MethodData {

    String methodName;
    String parameterTypes;
    String params;


    public MethodData(Method method, Object... params) {
        this.methodName = method.getName();
        this.parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList()).toString();
        this.params = JSON.toJSONString(params, SerializerFeature.WriteClassName);
    }


    public MethodData(JsonObject jsonObject) {
        this.methodName = jsonObject.getString("method");
        this.parameterTypes = jsonObject.getString("paramType");
        this.params = jsonObject.getString("params");
    }

    public String toKey() {
        return BaseProxyProcessor.toKey(this.methodName, this.parameterTypes);
    }

    public JsonObject toJson() {
        return new JsonObject().put("method", methodName).put("paramType", parameterTypes).put("params", params);
    }

    public ReturnData apply(ClassData classData) throws Throwable {
        classData = ClassData.getData(classData);
        String key = toKey();
        Executor executor = classData.getExecutor();
        Class<?>[] aClass = executor._apply002(key);
        JSONArray array = JSON.parseArray(params);
        return new ReturnData(aClass[aClass.length - 1], executor._apply001(key, classData.getBean(), getParams(array, aClass)));
    }

    protected Object[] getParams(JSONArray array, Class<?>[] aClass) {
        Object[] params = new Object[aClass.length];
        Object o;
        for (int i = 0; i < aClass.length - 1; i++) {
            o = array.get(i);
            if (o == null) {
                continue;
            }
            params[i] = TypeUtils.castToJavaBean(o, aClass[i]);
        }
        return params;
    }

}
