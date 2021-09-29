package com.hubidaauto.servmarket.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;
import org.welisdoon.common.ObjectUtils;

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
}
