package org.welisdoon.model.data.consts;


import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class FeildLevelDefine {

    final static Map<String, FieldLevel> map = new HashMap<>();


    final public static void add(String s, FieldLevel level) {
        Assert.isTrue(!map.containsKey(s), String.format("keyword %s is define:%s", s, map.get(s).name()));
        Assert.isTrue(level.level != null, String.format("value %s is define:%s", level.name(), level.level));

        map.put(s, level);
        level.level = s;
    }

    final public static FieldLevel get(String key) {
        return map.get(key);
    }

    final public static String get(FieldLevel value) {
        return map.entrySet().stream().filter(entry ->
                entry.getValue().equals(value)
        ).map(Map.Entry::getKey).findFirst().orElse(null);
    }
}
