package org.welisdoon.web.vertx.proxy.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * @Classname ThreadLocalData
 * @Description TODO
 * @Author Septem
 * @Date 11:02
 */

@DataObject
public class ThreadLocalData {
    static Map<String, ThreadLocal<?>> threadLocals = new HashMap<>();

    public ThreadLocalData() {
    }

    static void add(String key, ThreadLocal<?> threadLocal) {
        threadLocals.put(key, threadLocal);
    }

    public ThreadLocalData(JsonObject jsonObject) {
        for (Map.Entry<String, Object> entry : jsonObject) {
            try {
                JsonObject jsonObject1 = ((JsonObject) entry.getValue());
                ObjectData objectData = new ObjectData(jsonObject1);
                ((ThreadLocal) threadLocals.get(entry.getKey())).set(objectData.toBean());
            } catch (IllegalStateException e) {
                throw e;
            } catch (Throwable e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        threadLocals.forEach((s, threadLocal) -> {
            if (threadLocal.get() == null) return;
            ObjectData objectData = new ObjectData(threadLocal.get());
            jsonObject.put(s, objectData.toJson());
        });
        return jsonObject;
    }
}
