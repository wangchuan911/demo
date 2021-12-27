package org.welisdoon.web.entity.wechat.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.reflections.ReflectionUtils;

import javax.xml.bind.annotation.XmlElement;

/**
 * @Classname SignUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/27 20:14
 */
public interface SignUtils {

    static String sign(Object object, String key) {
        StringBuilder tmpStr = new StringBuilder();
        Class<XmlElement> annotationClass = XmlElement.class;
        ReflectionUtils.getFields(object.getClass(), ReflectionUtils.withAnnotation(annotationClass))
                .stream()
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(object) != null;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t);
                    }
                })
                .sorted((o1, o2) -> {
                    /*char c1 = o1.getAnnotation(annotationClass).name().charAt(0);
                    char c2 = o2.getAnnotation(annotationClass).name().charAt(0);
                    return c1 - c2;*/
                    return ASCIISort(o1.getAnnotation(annotationClass).name(), o2.getAnnotation(annotationClass).name());
                })
                .forEachOrdered(field -> {
                    try {
                        tmpStr.append(field.getAnnotation(annotationClass).name()).append('=').append(field.get(object)).append('&');
                    } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t);
                    }
                });
        tmpStr.append("key=")
                .append(key);
        return DigestUtils
                .md5Hex(tmpStr.toString())
                .toUpperCase();
    }

    static int ASCIISort(String str1, String str2) {
        char c1;
        char c2;
        int v = 0, len = Math.min(str1.length(), str2.length());
        for (int idx = 0; idx < len; idx++) {
            c1 = str1.charAt(idx);
            c2 = str2.charAt(idx);
            v = c1 - c2;
            if (v == 0) {
                continue;
            }
            return v;
        }
        return v;
    }
}
