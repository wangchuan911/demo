package com.hubidaauto.servmarket.module.message.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * @Classname EventMessageBody
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/8 02:14
 */
public interface EventMessageBody {
    static MagicKey getMagic(String value) {
        return MagicKey.getInstance(value);
    }

    String toBodyString();

    enum MagicKey {
        DATE("@date@", (o) -> {
            if (o == null) return "";
            Date date = TypeUtils.castToDate(o);
            return new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(date);
        }), MONEY("@money@", (o) -> {
            if (o == null) return "0.00元";
            StringBuilder sb = new StringBuilder(o.toString());
            sb.insert(sb.length() - 2, ".");
            return (o != null ? sb.toString() : "0.00") + "元";
        }), NONE("", (o) ->
                o != null ? o.toString() : ""
        );
        String key;
        Function<Object, String> function;

        MagicKey(String key, Function<Object, String> function) {
            this.key = key;
            this.function = function;
        }

        public String getValue(String key) {
            if (StringUtils.isEmpty(this.key)) return key;
            return key.substring(this.key.length());
        }

        public String format(Object object) {
            return function.apply(object);
        }

        static MagicKey getInstance(String keyWord) {
            return Arrays.stream(values()).filter(magicKey -> magicKey != NONE && keyWord.startsWith(magicKey.key)).findFirst().orElse(NONE);
        }
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
