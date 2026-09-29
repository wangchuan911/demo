package org.welisdoon.common.object.wrapper;

import org.reflections.Reflections;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.common.object.PageIterator;

import java.lang.annotation.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname IObject
 * @Description TODO
 * @Author Septem
 * @Date 9:28
 */
public interface IDataAccessObject {

    Map<Class<?>, IDataAccessObject.BaseObjectInfo> CLASS_METHOD = new HashMap<>();

    static void initialization(Reflections reflections) {
        initialization((Collection) reflections.getTypesAnnotatedWith(DataFuture.class));
    }

    static void initialization(Collection<Class<? extends IDataAccessObject>> classes) {
        classes.forEach(IDataAccessObject::initialization);
    }

    static Class<?> initialization(Class<?> aClass) {
        if (!IDataAccessObject.class.isAssignableFrom(aClass) || aClass == AbstractDataAccessObject.class) return null;
        if (CLASS_METHOD.containsKey(aClass)) return aClass;
        Class<?> parent = initialization(aClass.getSuperclass());
        if (Arrays.stream(aClass.getInterfaces()).anyMatch(IDataAccessObject.class::isAssignableFrom)) {
            IDataAccessObject.BaseObjectInfo map = new SqlMapper.DataAccessObjectInfo(aClass, (SqlMapper.DataAccessObjectInfo) CLASS_METHOD.get(parent));
            CLASS_METHOD.put(aClass, map);
            return aClass;
        }
        return parent;
    }

    void initialization(Map<String, Prepare.Result> values);

    default <T> T getValue(String key) {
        if (isLazy()) {
            return loadRemote(key);
        } else {
            return loadLocal(key);
        }
    }

    default <T> List<T> getValues(String key) {
        if (isLazy()) {
            return loadsRemote(key);
        } else {
            return loadsLocal(key);
        }
    }

    default boolean isLazy() {
        return true;
    }

    <T> T loadRemote(String key);

    <T> List<T> loadsRemote(String key);


    <T> T loadLocal(String key);

    <T> List<T> loadsLocal(String key);

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DataFuture {
        String name();

        FutureExpress[] filter();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface FutureExpress {
        String property();

        String[] values();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tables {
        Table[] value();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Tables.class)
    @interface Table {

        Column[] columns();

        String table();

        String datasource();

        String[] filter() default {};

        String group() default "";

        TableRel rel() default TableRel.Strong;

    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Column {
        String column();

        String property() default "";

        String linkColumn() default "";

        ColumnType type() default ColumnType.Simple;

        Class<?> dataType() default String.class;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface ColMapper {
        String target();

        String column();
    }

    enum ColumnType {
        PrimaryKey, Simple, Virtual, ForeignKey;
    }

    /*@Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Field {
        String name();

        String column();

        Class<?> type();
    }

    @interface Foreign {
        String name();

        String[] column();

        Class<?> type();

        Class<? extends ObjectValueSupplier<?>> get();

        boolean isArray();
    }*/

    @FunctionalInterface
    interface ObjectValueSupplier<T> {
        List<T> get(IDataAccessObject object, Map<String, Object> params);
    }

    enum TableRel {
        Strong, Weak, Multi;
    }

    interface ObjectScanner {
        Pattern COLUMN = Pattern.compile("(\\w+)\\.(\\w+)");
        Pattern TABLE = Pattern.compile("(\\w+\\.\\w+)\\s+(\\w+)");

        void scanTableAnnotation();

        static Matcher getColumnMatcher(String express) {
            Matcher matcher = IDataAccessObject.ObjectScanner.COLUMN.matcher(express);
            matcher.find();
            return matcher;
        }

        static Matcher getTableMatcher(String express) {
            Matcher tableMatcher = IDataAccessObject.ObjectScanner.TABLE.matcher(express);
            tableMatcher.find();
            return tableMatcher;
        }
    }

    abstract class BaseObjectInfo<T extends BaseObjectInfo> {
        final protected Class<?> wrapper;
        final protected Class<? extends IDataAccessObject> target;
        final protected T parent;

        public BaseObjectInfo(Class<?> wrapper, T parent) {
            this.wrapper = wrapper;
            this.target = (Class<? extends IDataAccessObject>) Arrays.stream(wrapper.getInterfaces()).filter(IDataAccessObject.class::isAssignableFrom).findFirst().orElseThrow(() -> new IllegalStateException("没有继承[IDataAccessObject]接口"));
            this.parent = parent;
        }

        abstract List<IDataAccessObject> query(Map<String, Object> params);

        abstract List<IDataAccessObject> page(Map<String, Object> params, BaseCondition.Page page);

        abstract IDataAccessObject query(Object id);
    }

    default ValuesIterator find(Map<String, Object> params) {
        return new ValuesIterator(this.getClass(), params);
    }

    static List<IDataAccessObject> page(Class<? extends IDataAccessObject> aClass, Map<String, Object> params, BaseCondition.Page page) {
        initialization(aClass);
        BaseObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return Collections.emptyList();
        return dataAccessObjectInfo.page(params, page);
    }

    static <O extends IDataAccessObject> Optional<O> getOrDefault(Class<? extends IDataAccessObject> aClass, Object id) {
        return Optional.ofNullable(get(aClass, id));
    }

    static <O extends IDataAccessObject> O get(Class<? extends IDataAccessObject> aClass, Object id) {
        initialization(aClass);
        BaseObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return null;
        return (O) dataAccessObjectInfo.query(id);
    }

    static int count(Class<? extends IDataAccessObject> aClass, Map<String, Object> params, BaseCondition.Page page) {
        BaseObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return 0;
        dataAccessObjectInfo.query(params);

        return 1;
    }

    class ValuesIterator extends PageIterator<IDataAccessObject> {
        final Map<String, Object> params;
        final Class<? extends IDataAccessObject> aClass;

        public ValuesIterator(Class<? extends IDataAccessObject> aClass, Map<String, Object> params) {
            super(pager -> page(aClass, params, pager));
            this.aClass = aClass;
            this.params = params;
        }
    }

}
