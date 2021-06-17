package org.welisdoon.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @Classname ObjectUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/4/23 10:17
 */
public class ObjectUtils {
    @FunctionalInterface
    public interface newObjectFunction<T> {
        T create();
    }

    public static <K, V> V getMapValueOrNew(Map<K, V> map, K key, newObjectFunction<V> function) {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    V v = function.create();
                    map.put(key, v);
                }
            }
        }
        return map.get(key);
    }

    public static Class<?>[] getGenericTypes(Class<?> type) {
        Type superClass = type.getGenericSuperclass();
        Type[] t = ((ParameterizedType) superClass).getActualTypeArguments();
        return Arrays.stream(t).map(type1 -> {
            if (type1 instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type1).getRawType();
            } else if (type1 instanceof Class) {
                return (Class<?>) type1;
            } else {
                System.out.println(type1);
                return null;
            }
        }).toArray(Class[]::new);

    }

    @FunctionalInterface
    public static interface MapHandler<T> {
        T handle();
    }

    public static <K, V> V getObjInMapSafe(Map<K, V> map, K key, MapHandler<V> mapHandler) {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    V value = mapHandler.handle();
                    map.put(key, value);
                }
            }
        }
        return map.get(key);
    }

    public static void main(String[] args) throws InterruptedException {
        Map map = new HashMap<String, Integer>();
        /*final int[] j = {0};
        int[] i=new int[1000];
        Arrays.stream(i).parallel().forEach(value -> {
            int jj = j[0]++;
            System.out.println(Thread.currentThread().getName());
            System.out.println(jj);
        });
        Thread.sleep(5000);
        System.out.println(map);
        System.out.println(map.size());*/
        System.out.println(Arrays.toString(getGenericTypes(map.getClass())));
        ;
    }
}
