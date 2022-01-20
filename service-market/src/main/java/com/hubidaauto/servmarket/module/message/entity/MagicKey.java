package com.hubidaauto.servmarket.module.message.entity;

/**
 * @Classname MagicKey
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/20 12:27
 */

import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

public enum MagicKey {
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

    public static MagicKey getInstance(String keyWord) {
        return Arrays.stream(values()).filter(magicKey -> magicKey != NONE && keyWord.startsWith(magicKey.key)).findFirst().orElse(NONE);
    }
    public static MagicKey getMagic(String value) {
        return MagicKey.getInstance(value);
    }
}