package org.welisdoon.common.object.wrapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.common.object.PageIterator;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname IObject
 * @Description TODO
 * @Author Septem
 * @Date 9:28
 */
public interface IDataAccessObject {

    Map<Class<?>, DataAccessObjectInfo> CLASS_METHOD = new HashMap<>();

    static void initialization(Reflections reflections) {
        initialization((Collection) reflections.getTypesAnnotatedWith(DataFuture.class));
    }

    static void initialization(Collection<Class<? extends IDataAccessObject>> classes) {
        classes.forEach(IDataAccessObject::initialization);
    }

    static Class<?> initialization(Class<?> aClass) {
        if (!IDataAccessObject.class.isAssignableFrom(aClass)) return null;
        if (CLASS_METHOD.containsKey(aClass)) return aClass;
        Class<?> parent = initialization(aClass.getSuperclass());
        if (Arrays.stream(aClass.getInterfaces()).anyMatch(IDataAccessObject.class::isAssignableFrom)) {
            DataAccessObjectInfo map = new DataAccessObjectInfo(aClass, CLASS_METHOD.get(parent));
            CLASS_METHOD.put(aClass, map);
            return aClass;
        }
        return parent;
    }

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

        String[] filter();
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

        Class<?> input() default String.class;
    }

    enum ColumnType {
        PrimaryKeu, Simple, Virtual, ForeignKey;
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Field {
        String name();

        String[] columns();

        Class<Function> handler();

        Class<?>[] type();

        String localFieldName() default "";
    }

    enum TableRel {
        Strong, Weak, Multi;
    }

    class Model {
        public static class TableVO {
            final ColumnVO[] columns;
            final String table;
            final String datasource;
            final String[] filter;
            final String group;
            final TableRel rel;

            public TableVO(ColumnVO[] columns, String table, String datasource, String[] filter, String group, TableRel rel) {
                this.columns = columns;
                this.table = table;
                this.datasource = datasource;
                this.filter = filter;
                this.group = group;
                this.rel = rel;
            }

            public TableVO(Table table) {
                this(Arrays.stream(table.columns()).map(ColumnVO::new).toArray(ColumnVO[]::new), table.table(), table.datasource(), table.filter(), table.group(), table.rel());
            }

            public static class ColumnVO {
                final String column;
                final String property;
                final String linkColumn;
                final ColumnType type;
                final Class<?> input;

                public ColumnVO(String column, String property, String linkColumn, ColumnType type, Class<?> input) {
                    this.column = column;
                    this.property = property;
                    this.linkColumn = linkColumn;
                    this.type = type;
                    this.input = input;
                }

                ColumnVO(Column column) {
                    this(column.column(), column.property(), column.linkColumn(), column.type(), column.input());
                }
            }
        }
    }

    interface ObjectScanner {
        Pattern COLUMN = Pattern.compile("(\\w+)\\.(\\w+)");
        Pattern TABLE = Pattern.compile("(\\w+\\.\\w+)\\s+(\\w+)");

        default SqlMapper.GroupTable sql(SqlMapper mapper, String group, List<Model.TableVO> iterator, AtomicInteger index) {
            List<SqlMapper.AbstractTable> iTables = new LinkedList<>();
            TableRel rel = null;
            for (int i = 0; iterator.size() > index.get(); index.incrementAndGet(), i++) {
                Model.TableVO tableAnnotation = iterator.get(index.get());
                if (StringUtils.isEmpty(tableAnnotation.group) || tableAnnotation.group.equals(group)) {
                    Matcher tableMatcher = TABLE.matcher(tableAnnotation.table);
                    tableMatcher.find();
                    boolean changeRel = i == 0 && StringUtils.isNotEmpty(tableAnnotation.group);
                    if (changeRel) rel = tableAnnotation.rel;
                    List<SqlMapper.ColumnArg> columns = Arrays.stream(tableAnnotation.columns).map(column -> {
                        Matcher matcher = COLUMN.matcher(column.column);
                        if (!matcher.find()) {
                            return null;
                        }
                        return new SqlMapper.ColumnArg(matcher.group(2), column.property, column.linkColumn);
                    }).filter(Objects::nonNull).collect(Collectors.toList());
                    SqlMapper.BaseTable LeafTable = new SqlMapper.BaseTable(
                            tableMatcher.group(1),
                            tableMatcher.group(2),
                            columns,
                            tableAnnotation.filter,
                            changeRel ? TableRel.Strong : tableAnnotation.rel);
                    iTables.add(LeafTable);
                } else if (group == null || tableAnnotation.group.startsWith(group)) {
                    SqlMapper.AbstractTable table = sql(mapper, tableAnnotation.group, iterator, index);
                    index.decrementAndGet();
                    iTables.add(table);
                } else {
                    break;
                }
            }
            if (CollectionUtils.isEmpty(iTables)) return null;
            rel = rel != null ? rel : TableRel.Strong;
            SqlMapper.AbstractTable[] tables = iTables.toArray(SqlMapper.AbstractTable[]::new);
            if (group == null)
                return new SqlMapper.MainTable("ROOT", tables, rel);
            else
                return new SqlMapper.GroupTable(group, tables, rel);
        }

        void scanTableAnnotation(List<Model.TableVO> tableList);

        default SqlMapper.MainTable sql(SqlMapper mapper) {
            List<Model.TableVO> tables = new LinkedList<>();
            scanTableAnnotation(tables);
            SqlMapper.MainTable table = (SqlMapper.MainTable) sql(mapper, null, tables, new AtomicInteger(0));
            if (table == null) return null;
            return table;
        }
    }

    class DataAccessObjectInfo implements ObjectScanner {
        final protected Class<?> target;
        final protected DataAccessObjectInfo parent;
        protected Map<String, Method> methodMap;

        public DataAccessObjectInfo(Class<?> target, DataAccessObjectInfo parent) {
            this.target = target;
            this.parent = parent;
            this.methodMap = new HashMap<>();
            Arrays.stream(this.target.getInterfaces()).filter(IDataAccessObject.class::isAssignableFrom).flatMap(aClass -> Arrays.stream(aClass.getMethods())).forEach(method -> {
                Field column = method.getAnnotation(Field.class);
                if (column == null) return;
                methodMap.put(column.name(), method);
            });
        }

        public Method getMethod(String name) {
            Method method = methodMap.get(name);
            if (method == null && parent != null) {
                return parent.getMethod(name);
            }
            return method;
        }

        public void scanTableAnnotation(List<Model.TableVO> tableList) {
            if (parent != null) {
                parent.scanTableAnnotation(tableList);
            }
            Arrays.stream(target.getInterfaces()).filter(IDataAccessObject.class::isAssignableFrom).flatMap(aClass -> {
                Table table = aClass.getAnnotation(Table.class);
                if (table != null) return Stream.of(table);
                Tables tables = aClass.getAnnotation(Tables.class);
                if (tables != null) return Arrays.stream(tables.value());
                return Stream.of();
            }).map(Model.TableVO::new).forEach(tableList::add);
        }


    }

    default ValuesIterator find(Map<String, Object> params) {
        return new ValuesIterator(this.getClass(), params);
    }

    static List<IDataAccessObject> page(Class<? extends IDataAccessObject> aClass, Map<String, Object> params, BaseCondition.Page page) {
        initialization(aClass);
        DataAccessObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return Collections.emptyList();
        SqlMapper mapper = new SqlMapper();
        SqlMapper.MainTable table = dataAccessObjectInfo.sql(mapper);
        mapper.build(table, params);
        System.out.println(mapper.prepare.sql);
        return Collections.emptyList();
    }

    static <O extends IDataAccessObject> Optional<O> getOptional(Class<? extends IDataAccessObject> aClass, Object id) {
        return Optional.ofNullable(get(aClass, id));
    }

    static <O extends IDataAccessObject> O get(Class<? extends IDataAccessObject> aClass, Object id) {
        if (id instanceof Map) {
        }
        initialization(aClass);
        DataAccessObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return null;
        SqlMapper mapper = new SqlMapper();
        SqlMapper.MainTable table = dataAccessObjectInfo.sql(mapper);
        mapper.build(table, id);
        System.out.println(mapper.prepare.sql);
        return null;
    }

    static int count(Class<? extends IDataAccessObject> aClass, Map<String, Object> params, BaseCondition.Page page) {
        DataAccessObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return 0;
        SqlMapper mapper = new SqlMapper();
        SqlMapper.MainTable table = dataAccessObjectInfo.sql(mapper);
        mapper.build(table, params);

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
