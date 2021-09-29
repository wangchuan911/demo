package org.welisdoon.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @Classname ObjectUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/4/23 10:17
 */
public class ObjectUtils {

    final static ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static <K, V> V getMapValueOrNewSafe(Map<K, V> map, K key, Supplier<V> function) {
        if (!map.containsKey(key)) {
            REENTRANT_LOCK.lock();
            try {
                if (!map.containsKey(key)) {
                    V v = function.get();
                    map.put(key, v);
                }
            } finally {
                REENTRANT_LOCK.unlock();
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
