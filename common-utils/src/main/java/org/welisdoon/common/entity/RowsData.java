package org.welisdoon.common.entity;

import org.welisdoon.common.ObjectUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @Classname RowsData
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/5/18 10:40
 */
public class RowsData<E> {
    final static Map<Class<?>, DataCache> classMap = new HashMap<>();
    Object[][] rows;
    Header[] headers;

    public Object[] getRows() {
        return rows;
    }

    public void setRows(Object[][] rows) {
        this.rows = rows;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public RowsData(Collection<E> collections, Class<E> eClass) throws Throwable {
        DataCache cache;
        /*if (!classMap.containsKey(collections.getClass())) {
            synchronized (classMap) {
                if (!classMap.containsKey(collections.getClass())) {
                    classMap.put(collections.getClass(), new DataCache(eClass));
                }
            }
        }
        cache = classMap.get(collections.getClass());*/
        cache = ObjectUtils.getMapValueOrNewSafe(classMap, eClass, () -> new DataCache(eClass));
        this.headers = cache.headers;
        this.rows = new Object[collections.size()][];
        this.rows = collections.stream().map(item ->
                Arrays.stream(cache.fields).map(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(item);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toArray()
        ).toArray(Object[][]::new);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RowsDataHeader {
        String name();

        int order() default 0;

        boolean hidden() default false;
    }

    private static class DataCache {
        Field[] fields;
        Header[] headers;

        DataCache(Class<?> type) {
            this.fields = Arrays.stream(type
                    .getDeclaredFields())
                    .filter(field ->
                            field.getAnnotation(RowsDataHeader.class) != null
                    ).sorted(Comparator.comparingInt(o -> o.getAnnotation(RowsDataHeader.class).order())
                    ).toArray(Field[]::new);

            this.headers = Arrays.stream(fields)
                    .map(field -> {
                        RowsDataHeader rowsDataHeader = field.getAnnotation(RowsDataHeader.class);
                        return new Header(field.getName(), rowsDataHeader.name(), rowsDataHeader.hidden());
                    }).toArray(Header[]::new);
        }
    }

    public static class Header {
        String key;
        String name;
        boolean hidden;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Header(String key, String name, boolean hidden) {
            this.key = key;
            this.name = name;
            this.hidden = hidden;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }
    }

    private static class Foo {
        @RowsDataHeader(name = "a")
        String a;
        @RowsDataHeader(name = "b")
        String b;

        public Foo(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    public static void main(String[] args) throws Throwable {
        List<Foo> integers = new LinkedList<>();
        integers.add(new Foo("a1", "b1"));
        integers.add(new Foo("a2", "b2"));
        RowsData r = new RowsData(integers, Foo.class);
        System.out.println(r);
    }
}
