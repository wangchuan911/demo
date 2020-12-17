package org.welisdoon.webserver.common;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname JNIFieldDescriptors
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2020/12/17 10:55
 */
public interface JNIFieldDescriptors {
    Map<Class<?>, String> JAVA_BASE_TYPE = Map.of(
            int.class, "I",
            short.class, "S",
            boolean.class, "Z",
            byte.class, "B",
            long.class, "J",
            float.class, "F",
            double.class, "D",
            char.class, "C",
            void.class, "V"
    );

    static String TO_JNI_STRING(Method method) {
        return String.format("(%s)%s", Arrays.stream(method.getParameterTypes()).map(aClass ->
                CONVERT(aClass)
        ).collect(Collectors.joining("")), CONVERT(method.getReturnType()));
    }

    static String CONVERT(Class aClass) {
        if (JAVA_BASE_TYPE.containsKey(aClass)) {
            return JAVA_BASE_TYPE.get(aClass);
        } else {
            String name = aClass.getName();
            boolean isArray = name.charAt(0) == '[';
            return String.format("%s%s;", (isArray ? "" : "L"), name.replaceAll("\\.", "/"));
        }
    }


}
