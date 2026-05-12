package org.welisdoon.web.vertx.proxy.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.ClassUtils;

/**
 * @Classname InvokerMetaData
 * @Description TODO
 * @Author Septem
 * @Date 20:07
 */
@DataObject
public class ReturnData {
    String returnType;
    String value;


    public ReturnData(Class<?> returnType, Object value) {
        this.returnType = returnType.getName();
        if (value != null) {
            if (ClassUtils.isPrimitiveOrWrapper(returnType)) {
                this.value = value.toString();
            } else {
                this.value = JSON.toJSONString(value, SerializerFeature.WriteClassName);
            }
        }
    }

    public String getReturnType() {
        return returnType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object toVal() throws ClassNotFoundException {
        if (value == null || "NULL".equalsIgnoreCase(value)) {
            return null;
        }
        Class<?> clazz = ClassData.getClass(returnType);
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return value;
        }
        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return TypeUtils.castToJavaBean(value, clazz);
        }
        Object o = JSON.parse(value);
        return o instanceof JSON ? ((JSONObject) o).toJavaObject(clazz) : value;
    }

    public ReturnData(JsonObject jsonObject) {
        this.returnType = jsonObject.getString("type");
        this.value = jsonObject.getString("val");
    }

    public JsonObject toJson() {
        return new JsonObject().put("type", returnType).put("val", value);
    }
}
