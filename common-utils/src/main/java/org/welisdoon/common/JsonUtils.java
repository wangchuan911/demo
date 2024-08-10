package org.welisdoon.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.sun.istack.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public interface JsonUtils {
    static Object getKeyValue(JSON json, String longKey) {
        if (StringUtils.isEmpty(longKey)) {
            return json;
        }
        Object val = json;
        for (String s : longKey.split("[\\.\\[\\]]+")) {
            if (val == null) return null;
            if (org.apache.commons.lang3.StringUtils.isEmpty(s)) continue;
            if (val instanceof JSONObject) {
                val = ((JSONObject) val).get(s);
            } else if (val instanceof JSONArray) {
                val = ((JSONArray) val).get(Integer.valueOf(s));
            } else {
                val = null;
            }
        }
        return val;
    }

    static <T> T getKeyValueToBean(JSON json, String longKey, Class<T> type) {
        Optional<Object> optional = Optional.ofNullable(getKeyValue(json, longKey));
        return optional.isEmpty() ? null : TypeUtils.castToJavaBean(optional.get(), type);
    }

    static <T> T toBean(@NotNull JSON json, Class<T> type) {
        return TypeUtils.castToJavaBean(json, type);
    }

    static <T> T toBean(@NotNull String json, Class<T> type) {
        return toBean((JSON) JSON.parse(json), type);
    }

    static String asJsonString(@NotNull Object json) {
        return JSON.toJSONString(json);
    }
}
