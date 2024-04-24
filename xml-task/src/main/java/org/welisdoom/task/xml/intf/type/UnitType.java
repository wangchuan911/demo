package org.welisdoom.task.xml.intf.type;


import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.entity.TaskInstance;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoom.task.xml.handler.OgnlUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname UnitType
 * @Description TODO
 * @Author Septem
 * @Date 9:30
 */
public interface UnitType {
    String getId();

    <T extends Unit> List<T> getChild(Class<T> tClass);

    <T extends Unit> List<T> getChild(Predicate<Unit> predicate);

    <T extends Unit> List<T> getChildren(Class<T> tClass);

    <T extends Unit> T getParent(Class<T> tClass);

    <T extends Unit> List<T> getParents(Class<T> tClass);

    <T extends Unit> T getParent(Predicate<Class<?>> predicate);

    <T extends Unit> List<T> getParents(Predicate<Class<?>> predicate);


    static Predicate<Unit> typeMatched(Class<?>... classes) {
        return unit -> Arrays.stream(classes).filter(aClass -> aClass.isAssignableFrom(unit.getClass())).findFirst().isPresent();
    }


    String DEFAULT_VALUE_SIGN = "\\{\\{(.+?)\\}\\}";
    Pattern PATTERN = Pattern.compile(DEFAULT_VALUE_SIGN);
    String sign = "%@#VALUE#@%";

    static String textFormat(TaskInstance request, String text) {
        return textFormat(request, request.getBus(), text);
    }

    static String textFormat(TaskInstance request, Map params, String text) {
        if (StringUtils.isEmpty(text)) return "";
        if (text.indexOf("{{") == -1) return text;
        List<Map.Entry<String, Object>> list = new LinkedList<>();
        Matcher matcher = PATTERN.matcher(text);
        String name;
        Map.Entry<String, Object> value;
        while (matcher.find()) {
            switch (matcher.groupCount()) {
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    continue;
            }
            try {
                value = Map.entry(name, OgnlUtils.getValue(name, request.getOgnlContext(), params, Object.class));
            } catch (Throwable e) {/*
                throw new RuntimeException(e.getMessage(), e);*/
                System.err.println(text + "===>" + name + "==>" + e.getMessage());
                value = Map.entry(name, "");
            }
            list.add(value);
        }
        text = text.replaceAll(DEFAULT_VALUE_SIGN, sign);
        int offset;
        for (Map.Entry<String, Object> entry : list) {
            offset = text.indexOf(sign);
            text = text.substring(0, offset) + TypeUtils.castToString(entry.getValue()) + text.substring(offset + sign.length());
//            text = text.replaceFirst(sign, TypeUtils.castToString(entry.getValue()));
        }
        return text;
    }

}
