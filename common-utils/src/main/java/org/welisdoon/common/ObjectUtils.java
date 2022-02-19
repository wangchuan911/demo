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
            Optional<Type> optionalTypes;
            while ((optionalTypes = Arrays.stream(type.getGenericInterfaces()).filter(type1 -> {
                /*if (type1 instanceof ParameterizedType) {
                    return ((ParameterizedType) type1).getRawType() == targetClass;
                } else if (type1 instanceof Class) {
                    return type1 == targetClass;
                }
                return false;*/
                return getTypeClass(type1) == targetClass;
            }).findFirst()).isEmpty() && (type = getTypeClass(type.getGenericSuperclass())) != Object.class) {
            }
            Type t1 = optionalTypes.orElse(Object.class);
            if (t1 instanceof ParameterizedType) {
                t = ((ParameterizedType) t1).getActualTypeArguments();
            } else {
                t = new Type[]{};
            }
        } else {
            Type superClass = type;
            while (superClass != null) {
                /*if (superClass instanceof ParameterizedType) {
                    if (((ParameterizedType) superClass).getRawType() == targetClass) break;
                    superClass = ((ParameterizedType) superClass).getRawType();
                } else if (superClass instanceof Class) {
                    if (superClass == targetClass) break;
                } else {
                    superClass = null;
                    break;
                }*/
                if (getTypeClass(superClass) == targetClass) break;
                superClass = getTypeClass(superClass).getGenericSuperclass();
            }
            if (superClass == Object.class)
                t = new Type[]{};
            else
                t = ((ParameterizedType) superClass).getActualTypeArguments();
        }
        return Arrays.stream(t).map(ObjectUtils::getTypeClass).toArray(Class[]::new);

    }

    static Class getTypeClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof Class) {
            return (Class<?>) type;
        } else {
            System.out.println(type);
            return Object.class;
        }
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
