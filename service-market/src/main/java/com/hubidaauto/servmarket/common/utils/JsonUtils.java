package com.hubidaauto.servmarket.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;
import org.welisdoon.common.ObjectUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Classname JsonUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 09:48
 */
public interface JsonUtils {

    static <T> T jsonToObject(String jsonStr, Class<T> type, Supplier<T> objectFunction) {
        if (StringUtils.isEmpty(jsonStr))
            return objectFunction.get();
        else
            return JSONObject.parseObject(jsonStr, type);
    }
    static void toLongKeyMap(Map<String, Object> map, String key, JSONObject jsonObject) {
        Object object, json;
        for (String s : jsonObject.keySet()) {
            object = jsonObject.get(s);
            if (object == null) continue;
            json = JSON.toJSON(object);
            if (json instanceof JSONObject) {
                toLongKeyMap(map, String.format("%s.%s", key, s), (JSONObject) json);
            } else if (json instanceof JSONArray) {
                toLongKeyMap(map, String.format("%s.%s", key, s), (JSONArray) json);
            } else if (json == object) {
                map.put(String.format("%s.%s", key, s), json);
            }
        }
    }

    static void toLongKeyMap(Map<String, Object> map, String key, JSONArray jsonArray) {
        Object object, json;
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            object = jsonArray.get(i);
            if (object == null) continue;
            json = JSON.toJSON(object);
            if (json instanceof JSONObject) {
                toLongKeyMap(map, String.format("%[%d]", key, i), (JSONObject) json);
            } else if (json instanceof JSONArray) {
                toLongKeyMap(map, String.format("%s[%d]", key, i), (JSONArray) json);
            } else if (json == object) {
                map.put(String.format("%s[%d]", key, i), json);
            }
        }
    }

    static Object toLongKeyMap(JSONObject map, String longKey) {
        int i = longKey.indexOf(".");
        Object object, json;
        String curKey = i < 0 ? longKey : longKey.substring(0, i);
        object = map.get(curKey);
        if (object == null) return null;
        json = JSON.toJSON(object);
        if (json instanceof JSONObject) {
            return toLongKeyMap((JSONObject) json, longKey.substring(i + 1));
        } else if (json instanceof JSONArray) {
            return toLongKeyMap((JSONArray) json, longKey.substring(i + 1));
        } else if (json == object) {
            return object;
        }
        return null;
    }

    static Object toLongKeyMap(JSONArray arr, String longKey) {
        int i = longKey.indexOf(".");
        Object object, json;
        String curKey = i < 0 ? longKey : longKey.substring(0, i);
        int index = Integer.valueOf(curKey.substring(1, curKey.length() - 1));
        if (index >= arr.size()) return null;
        object = arr.get(index);
        if (object == null) return null;
        json = JSON.toJSON(object);
        if (json instanceof JSONObject) {
            return toLongKeyMap((JSONObject) json, longKey.substring(i + 1));
        } else if (json instanceof JSONArray) {
            return toLongKeyMap((JSONArray) json, longKey.substring(i + 1));
        } else if (json == object) {
            return object;
        }
        return null;
    }
}
