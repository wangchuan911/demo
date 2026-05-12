package org.welisdoon.common;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * @Classname ObjectUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/4/23 10:17
 */
public class ObjectUtils {
    @FunctionalInterface
    public interface IfNull<T, E extends Throwable> {
        T get() throws E;
    }

    final static ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static <K, V, E extends Throwable> V getMapValueOrNewSafe(Map<K, V> map, K key, IfNull<V, E> function) throws E {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    V v = function.get();
                    map.put(key, v);
                }
            }
        }
        return map.get(key);
    }

    public static <T, K> K synchronizedGet(final T lockObject, final Function<T, K> get, final Function<T, K> create) {
        K k;
        if (Objects.isNull(k = get.apply(lockObject)))
            synchronized (lockObject) {
                if (Objects.isNull(k = get.apply(lockObject)))
                    return create.apply(lockObject);
            }
        return k;
    }

    public static <T> void synchronizedInitial(final T lockObject, final Predicate<T> get, final Consumer<T> create) {
        if (!get.test(lockObject))
            synchronized (lockObject) {
                if (!get.test(lockObject))
                    create.accept(lockObject);
            }
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

    public static Class getTypeClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof Class) {
            return (Class<?>) type;
        } else {
            System.out.println(type);
            return Object.class;
        }
    }


    public static void main(String[] args) throws ClassNotFoundException {
//        SpringApplication.run(WebserverApplication.class, args);
        /*Arrays.stream(ObjectUtils.class.getMethods()).filter(method -> method.getName().equals("a")).forEach(method -> {
            System.out.println(method.getName());
            System.out.println(Arrays.stream(method.getGenericParameterTypes()).map(Type::getTypeName).collect(Collectors.joining("\n")));
            System.out.println("#");
            System.out.println(Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining("\n")));
            System.out.println("#");
            Arrays.stream(method.getGenericParameterTypes()).forEach(type -> {
                System.out.println(getTypeClass(type).getName());
                System.out.println(new ObjectDefineInfo(type).toString());
                System.out.println();
            });
        });*/
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        arrangement(list, 3, integers -> {
            System.out.println(integers);
            return true;
        });
    }

    public static class ObjectDefineInfo {
        protected String name;
        protected ObjectDefineInfo[] innerTypes;
        protected ObjectDefineType type;

        public ObjectDefineInfo(Type type) {
            this.type = ObjectDefineType.getInstance(type);
            this.name = this.type.name.apply(type);
            this.innerTypes = Arrays.stream(this.type.subTypes.apply(type)).map(type1 -> new ObjectDefineInfo(type1)).toArray(ObjectDefineInfo[]::new);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ObjectDefineInfo.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("innerTypes=" + Arrays.toString(innerTypes))
                    .add("type=" + type)
                    .toString();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ObjectDefineInfo[] getInnerTypes() {
            return innerTypes;
        }

        public void setInnerTypes(ObjectDefineInfo[] innerTypes) {
            this.innerTypes = innerTypes;
        }

        public ObjectDefineType getType() {
            return type;
        }

        public void setType(ObjectDefineType type) {
            this.type = type;
        }
    }

    public enum ObjectDefineType {
        ObjectArray(t -> t instanceof Class && ((Class) t).isArray(), s -> ((Class) s).getName(), s -> new Type[]{}),
        TypeArray(s -> s instanceof GenericArrayType, s -> getTypeClass(s).getName(), s -> getInnerTypeClass(s)),
        Base(s -> s instanceof Class && List.of(byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class,
                char.class, Character.class, CharSequence.class).stream().filter(aClass -> aClass.isAssignableFrom((Class) s)).findFirst().isPresent(), s -> ((Class) s).getName(), s -> new Type[]{}),
        Types(s -> s instanceof ParameterizedType, s -> getTypeClass(s).getName(), ObjectDefineType::getInnerTypeClass),
        Interface(s -> s instanceof Class && ((Class) s).isInterface(), s -> getTypeClass(s).getName(), s -> new Type[]{}),
        Object(s -> s instanceof Class && !((Class) s).isInterface() || s instanceof WildcardType, s -> getTypeClass(s).getName(), s -> new Type[]{});

        Function<Type, Boolean> matched;
        Function<Type, String> name;
        Function<Type, Type[]> subTypes;

        ObjectDefineType(Function<Type, Boolean> matched, Function<Type, String> name, Function<Type, Type[]> subTypes) {
            this.matched = matched;
            this.name = name;
            this.subTypes = subTypes;


        }

        public static ObjectDefineType getInstance(Type s) {
            return Arrays.stream(values()).filter(paramType -> paramType.matched.apply(s)).findFirst().get();
        }

        static Class getTypeClass(Type type) {
            if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            } else if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof GenericArrayType) {
                StringBuilder name = new StringBuilder("[");
                while ((type = ((GenericArrayType) type).getGenericComponentType()) instanceof GenericArrayType) {
                    name.append("[");
                }
                name.append("L").append(getTypeClass(type).getName()).append(";");
                try {
                    return Class.forName(name.toString());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.out.println(type);
                return Object.class;
            }
        }


        static Type[] getInnerTypeClass(Type type) {
            if (type instanceof ParameterizedType) {
                return Arrays.stream(((ParameterizedType) type).getActualTypeArguments()).toArray(Type[]::new);
            } else if (type instanceof GenericArrayType) {
                return getInnerTypeClass(((GenericArrayType) type).getGenericComponentType());
            } else {
                System.out.println(type);
                return new Type[]{};
            }
        }

    }

    public static <T extends Annotation> T[] getAnnotations(Class<?> aClass, Class<T> annotationClass) {
        T[] ts = aClass.getAnnotationsByType(annotationClass);

        if (ts == null) {
            try {
                Annotation annotation = aClass.getAnnotation(annotationClass.getAnnotation(Repeatable.class).value());
                ts = (T[]) annotation.getClass().getMethod("value").invoke(annotation);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                ts = (T[]) Array.newInstance(annotationClass, 0);
            }
        }
        return ts;
    }

    public static <T extends Annotation, O> T[] getAnnotations(Class<? extends O> targetClass, Class<T> annotationClass, Class<O> originClass) {
        List<T> list = new LinkedList<>();
        T[] ts;
        Class<?> aClass = targetClass;
        while (originClass.isAssignableFrom(aClass)) {
            ts = getAnnotations(aClass, annotationClass);
            if (ts != null && ts.length > 0) {
                for (T t : ts) {
                    list.add(t);
                }
                break;
            }
            aClass = aClass.getSuperclass();
        }
        return list.toArray((T[]) Array.newInstance(annotationClass, 0));
    }


    public static <T> void arrangement(List<T> datas, int count, Function<List<T>, Boolean> consumer) {
        arrangement(datas, new ArrayList<>(count + 1), count, 0, 0, consumer);
    }

    protected static <T> boolean arrangement(List<T> datas, List<T> ts, int count, int offset, int index, Function<List<T>, Boolean> consumer) {
        boolean flag;
        for (int i = index; i < datas.size() - count + offset + 1; i++) {
            if (ts.size() >= offset + 1) {
                ts.set(offset, datas.get(i));
            } else {
                ts.add(datas.get(i));
            }
            if (offset + 1 == count) {
                flag = consumer.apply(ts);
            } else {
                flag = arrangement(datas, ts, count, offset + 1, i + 1, consumer);
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    public static <T> LoopState find(T data, Function<T, List<T>> function, Function<T, LoopState> resultConsumer) {
        for (T t : function.apply(data)) {
            if (find(t, function, resultConsumer) == LoopState.Break) {
                return LoopState.Break;
            }
        }
        resultConsumer.apply(data);
        return LoopState.Continue;
    }

    public static <T> LoopState find(Supplier<T> supplier, Function<T, LoopState> resultConsumer) {
        T t;
        while (Objects.nonNull(t = supplier.get())) {
            if (resultConsumer.apply(t) == LoopState.Break) {
                return LoopState.Break;
            }
        }
        return LoopState.Continue;
    }

    public enum LoopState {
        Continue, Break;
    }

    public static <T> boolean find(Iterator<T> datas, Function<Node<T>, Iterator<T>> childrenIterator, Function<Node<T>, Boolean> resultConsumer) {
        return find(null, datas, childrenIterator, resultConsumer);
    }

    public static <T> boolean find(Node<T> parent, Iterator<T> datas, Function<Node<T>, Iterator<T>> childrenIterator, Function<Node<T>, Boolean> resultConsumer) {
        T data;
        Node<T> tNode;
        while (datas.hasNext()) {
            data = datas.next();
            tNode = new Node<>(data, childrenIterator, parent);
            if (!resultConsumer.apply(tNode) || !find(tNode, tNode.childrenIterator, childrenIterator, resultConsumer)) {
                return false;
            }
        }
        return true;
    }


    public static class Node<T> {
        final public T val;
        final public Iterator<T> childrenIterator;
        final public Node<T> parent;

        public Node(T val, Function<Node<T>, Iterator<T>> childrenIterator) {
            this(val, childrenIterator, null);
        }

        public Node(T val, Function<Node<T>, Iterator<T>> childrenIterator, Node<T> parent) {
            this.val = val;
            this.parent = parent;
            this.childrenIterator = childrenIterator.apply(this);
        }
    }
}
