package org.welisdoon.web.vertx.proxy.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @Classname ObjectData
 * @Description TODO
 * @Author Septem
 * @Date 8:59
 */

@DataObject
public class ObjectData {
    String className;
    String target;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ObjectData(Object o) {
        this.className = o.getClass().getName();
        this.target = JSON.toJSONString(o, SerializerFeature.WriteClassName);
    }

    public ObjectData(JsonObject jsonObject) {
        className = jsonObject.getString("clz");
        target = jsonObject.getString("tar");
    }

    public JsonObject toJson() {
        return new JsonObject().put("clz", className).put("tar", target);
    }

    public Object toBean() {
        Class aClass = ClassData.getClass(className);
        if (aClass.isPrimitive()) {
            return TypeUtils.cast(target, aClass, ParserConfig.getGlobalInstance());
        } else {
            return JSON.toJavaObject((JSON) JSON.toJSON(target), aClass);
        }
    }
}
