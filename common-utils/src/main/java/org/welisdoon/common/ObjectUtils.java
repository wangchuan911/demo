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

    public static Class<?> getGenericTypes(Class<?> type, Class<?> targetClass, int index) {
        Class[] cs = getGenericTypes(type, targetClass);
        if (cs.length < index + 1) {
            return null;
        }
        return cs[index];
    }

    public static Class<?>[] getGenericTypes(Class<?> type, Class<?> targetClass) {
        Type[] t;
        if (targetClass.isInterface()) {
            t = Arrays.stream(type.getGenericInterfaces()).filter(type1 -> {
                if (type1 instanceof ParameterizedType) {
                    return ((ParameterizedType) type1).getRawType() == targetClass;
                } else if (type1 instanceof Class) {
                    return type1 == targetClass;
                }
                return false;
            }).map(type1 -> {
                if (type1 instanceof ParameterizedType) {
                    return ((ParameterizedType) type1).getActualTypeArguments();
                } else {
                    return new Type[]{};
                }
            }).findFirst().orElseGet(() -> new Type[]{});
        } else {
            Type superClass = type;
            while (superClass != null) {
                if (superClass instanceof ParameterizedType) {
                    if (((ParameterizedType) superClass).getRawType() == targetClass) break;
                    superClass = ((ParameterizedType) superClass).getRawType();
                } else if (superClass instanceof Class) {
                    if (superClass == targetClass) break;
                } else {
                    superClass = null;
                    break;
                }
                superClass = ((Class) superClass).getGenericSuperclass();
            }
            if (superClass == null)
                t = new Type[]{};
            else
                t = ((ParameterizedType) superClass).getActualTypeArguments();
        }
        return Arrays.stream(t).map(type1 -> {
            if (type1 instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type1).getRawType();
            } else if (type1 instanceof Class) {
                return (Class<?>) type1;
            } else {
                System.out.println(type1);
                return Object.class;
            }
        }).toArray(Class[]::new);

    }


    public static void main(String[] args) throws InterruptedException {
        Map<String, Integer> map = new HashMap<String, Integer>();
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
        System.out.println(Arrays.toString(getGenericTypes(map.getClass(), Map.class)));
        ;
    }
}
