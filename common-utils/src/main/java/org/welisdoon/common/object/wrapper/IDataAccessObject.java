package org.welisdoon.common.object.wrapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.welisdoon.common.data.BaseCondition;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    class DataAccessObjectInfo {
        static final Pattern COLUMN = Pattern.compile("(\\w+)\\.(\\w+)");
        static final Pattern TABLE = Pattern.compile("(\\w+\\.\\w+)\\s+(\\w+)");
        final protected Class<?> target;
        final protected DataAccessObjectInfo parent;
        protected Map<String, Method> methodMap;

        public DataAccessObjectInfo(Class<?> target, DataAccessObjectInfo parent) {
            this.target = target;
            this.parent = parent;
            this.methodMap = new HashMap<>();
            Arrays.stream(this.target.getInterfaces()).filter(aClass -> IDemoTypeAEntity.class.isAssignableFrom(aClass)).flatMap(aClass -> Arrays.stream(aClass.getMethods())).forEach(method -> {
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

        protected List<Table> getTableAnnotation() {
            return Arrays.stream(target.getInterfaces()).filter(aClass -> IDataAccessObject.class.isAssignableFrom(aClass)).flatMap(aClass -> {
                Table table = aClass.getAnnotation(Table.class);
                if (table != null) return Stream.of(table);
                Tables tables = aClass.getAnnotation(Tables.class);
                if (tables != null) return Arrays.stream(tables.value());
                return Stream.of();
            }).collect(Collectors.toList());
        }

        SqlMapper.GroupTable sql(SqlMapper mapper) {
            SqlMapper.GroupTable tableParent = null;
            if (parent != null) {
                tableParent = parent.sql(mapper);
            }

            SqlMapper.GroupTable table = sql(mapper, null, getTableAnnotation(), new AtomicInteger(0));
            if (table == null) return null;
            if (tableParent != null) {
                table = mapper.new GroupTable(tableParent.name, tableParent, table);
            }
            mapper.init(table);
            return table;
        }

        SqlMapper.GroupTable sql(SqlMapper mapper, String group, List<Table> iterator, AtomicInteger index) {
            List<SqlMapper.ITable> iTables = new LinkedList<>();
            TableRel rel = null;
            for (int i = 0; iterator.size() > index.get(); index.incrementAndGet(), i++) {
                Table tableAnnotation = iterator.get(index.get());
                Matcher tableMatcher = TABLE.matcher(tableAnnotation.table());
                tableMatcher.find();
                if (StringUtils.isEmpty(tableAnnotation.group()) || tableAnnotation.group().equals(group)) {
                    boolean changeRel = i == 0 && StringUtils.isNotEmpty(tableAnnotation.group());
                    if (changeRel) rel = tableAnnotation.rel();
                    SqlMapper.Table LeafTable = mapper.new Table(
                            tableMatcher.group(1),
                            tableMatcher.group(2),
                            Arrays.stream(tableAnnotation.columns()).map(column -> {
                                Matcher matcher = DataAccessObjectInfo.COLUMN.matcher(column.column());
                                if (!matcher.find()) {
                                    return null;
                                }
                                return new SqlMapper.ColumnArg(matcher.group(2), column.property());
                            }).filter(Objects::nonNull).toArray(SqlMapper.ColumnArg[]::new),
                            tableAnnotation.filter(),
                            changeRel ? TableRel.Strong : tableAnnotation.rel());
                    for (Column link : tableAnnotation.columns()) {
                        if (StringUtils.isEmpty(link.linkColumn())) continue;
                        mapper.relColumn.add(Map.entry(link.column(), link.linkColumn()));
                    }
                    iTables.add(LeafTable);
                } else {
                    iTables.add(sql(mapper, tableAnnotation.group(), iterator, index));
                }
            }
            if (CollectionUtils.isEmpty(iTables)) return null;
            return mapper.new GroupTable(group, iTables, rel != null ? rel : TableRel.Strong);
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
        SqlMapper.GroupTable table = dataAccessObjectInfo.sql(mapper);
        mapper.build(table, params);

        return Collections.emptyList();
    }

    static int count(Class<? extends IDataAccessObject> aClass, Map<String, Object> params, BaseCondition.Page page) {
        DataAccessObjectInfo dataAccessObjectInfo = CLASS_METHOD.get(aClass);
        if (dataAccessObjectInfo == null) return 0;
        SqlMapper mapper = new SqlMapper();
        SqlMapper.GroupTable table = dataAccessObjectInfo.sql(mapper);
        mapper.build(table, params);

        return 1;
    }

    class ValuesIterator implements Iterator<IDataAccessObject> {
        Iterator<IDataAccessObject> iterator;
        final Map<String, Object> params;
        boolean more;
        final BaseCondition.Page page;
        final Class<? extends IDataAccessObject> aClass;
        int position = -1;

        public ValuesIterator(Class<? extends IDataAccessObject> aClass, Map<String, Object> params) {
            this.aClass = aClass;
            this.params = params;
            page = new BaseCondition.Page(1, 100);
        }

        @Override
        public boolean hasNext() {
            if (iterator == null) {
                iterator = page(aClass, params, page).iterator();
                more = iterator.hasNext();
            }
            if (!more) {
                return false;
            } else if (!iterator.hasNext()) {
                iterator = page(aClass, params, page.nextPage()).iterator();
                more = iterator.hasNext();
            }
            return iterator.hasNext();
        }

        @Override
        public IDataAccessObject next() {
            position++;
            return iterator.next();
        }

        public boolean hasMore() {
            return more;
        }

        public int getPosition() {
            return position;
        }

        public Spliterator<IDataAccessObject> spliterator() {
            return Spliterators.spliteratorUnknownSize(this, 0);
        }


        public Stream<IDataAccessObject> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        public Stream<IDataAccessObject> parallelStream() {
            return StreamSupport.stream(spliterator(), true);
        }
    }

    class SqlMapper {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new LinkedList<>();
        List<Map.Entry<String, String>> relColumn = new LinkedList<>();

        public void add(Table.Column column1, Table.Column column2) {
            column1.linkColumn(column2);
            column2.linkColumn(column1);
        }

        void init(GroupTable table) {
            table.initRelColumn(relColumn);
            relColumn.clear();
        }

        void build(GroupTable table, Map<String, Object> params) {
            sql.setLength(0);
            params.clear();
            List<Map.Entry<ITable, Map<String, Object>>> entries = Arrays.stream(table.tables).map(iTable -> {
                return Map.entry(iTable, params.entrySet().stream().filter(entry -> {
                    return iTable.getAliasColumn(entry.getKey()) != null;
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }).filter(iTableMapEntry -> MapUtils.isNotEmpty(iTableMapEntry.getValue())).collect(Collectors.toList());

            if (entries.isEmpty()) {
                MessageFormat.format("select {0} from {1} {2} where {3}",
                        Arrays.stream(table.columns).map(column -> MessageFormat.format("{0}.{1} as \"{2}\"", column.column.getTable().name, column.column.name, column.column.alias)).collect(Collectors.joining(",")),
                        table.tables[0].build(params, true),
                        Arrays.stream(table.tables).skip(1).map(iTable -> iTable.build(params, false))
                );
            } else {
                table.build(params);
            }
            Arrays.stream(table.columns).filter(column -> !column.column.linkColumns.isEmpty()).collect(Collectors.toList());
            /*String where = params.entrySet().stream().map(entry -> {
                ITable.IColumn column = main.getAliasColumn(entry.getKey());
                if (column == null) return null;
                Table.Column column1;
                if (column instanceof GroupTable.Column) {
                    column1 = ((GroupTable.Column) column).column;
                } else {
                    column1 = (Table.Column) column;
                }
                this.params.add(entry.getValue());
                return MessageFormat.format("{0}.{1}=?", column1.getTable().alias, column1.name);
            }).filter(Objects::nonNull).collect(Collectors.joining(" and "));
            Set<ITable> iTables = params.keySet().stream()
                    .map(s -> {
                                ITable.IColumn iColumn;
                                for (ITable iTable : table.tables) {
                                    if ((iColumn = iTable.getAliasColumn(s)) != null)
                                        return iColumn.getTable();
                                }
                                return null;
                            }
                    ).filter(Objects::nonNull).collect(Collectors.toSet());*/
        }

        //        String toSql(GroupTable groupTable) {
//            groupTable.tables.fo
//        }
        public static class ColumnArg {
            String name;
            String alias;

            public ColumnArg(String name, String alias) {
                this.name = name;
                this.alias = alias;
            }
        }

        public interface ITable {
            class SqlBuilder {

            }

            boolean match(String key);

            SqlBuilder build(Map<String, Object> map, boolean start);

            interface IColumn {
                void linkColumn(IColumn column);

                ITable getTable();
            }

            IColumn getColumn(String tableKey, String columnKey);

            IColumn getAliasColumn(String alias);

            void setParent(ITable parent);

            TableRel getRel();
        }

        public class GroupTable implements ITable {
            String name;
            final ITable[] tables;
            final TableRel rel;
            ITable parent;
            final Column[] columns;

            public GroupTable(String name, ITable[] iTables, TableRel rel) {
                this.name = name;
                this.tables = iTables;
                for (ITable iTable : iTables) {
                    iTable.setParent(this);
                }
                columns = Arrays.stream(tables).flatMap(iTable -> {
                    if (iTable instanceof GroupTable) {
                        return Arrays.stream(((GroupTable) iTable).columns);
                    } else if (iTable instanceof Table) {
                        return Arrays.stream(((Table) iTable).columns).map(column -> this.new Column(column));
                    } else {
                        return Stream.of();
                    }
                }).toArray(Column[]::new);
                this.rel = rel;
            }


            public GroupTable(String name, List<ITable> iTables, TableRel rel) {
                this(name, iTables.toArray(ITable[]::new), rel);
            }

            public GroupTable(String name, GroupTable... tables) {
                this.name = name;
                this.rel = tables[0].getRel();
                this.tables = Arrays.stream(tables).flatMap(groupTable -> Arrays.stream(groupTable.tables.clone())).toArray(ITable[]::new);
                this.columns = Arrays.stream(tables).flatMap(groupTable -> Arrays.stream(groupTable.columns)).toArray(Column[]::new);
            }

            public void setParent(ITable parent) {
                this.parent = parent;
            }

            void build(Map<String, Object> map) {

            }

            @Override
            public boolean match(String key) {
                for (ITable table : tables) {
                    if (table.match(key)) return true;
                }
                return false;
            }

            @Override
            public SqlBuilder build(Map<String, Object> map, boolean start) {
                return null;
            }


            @Override
            public TableRel getRel() {
                return rel;
            }

            public IColumn getColumn(String tableKey, String columnKey) {
                for (ITable table : tables) {
                    if (table instanceof GroupTable) {
                        IColumn iColumn = table.getColumn(tableKey, columnKey);
                        for (Column column : columns) {
                            if (column.equals(iColumn)) {
                                return column;
                            }
                        }
                    } else if (table instanceof Table && table.match(tableKey)) {
                        return ((Table) table).getColumn(columnKey);
                    }
                }
                return null;
            }

            @Override
            public IColumn getAliasColumn(String alias) {
                for (Column column : columns) {
                    if (alias.equals(column.column.alias)) return column;
                }
                return null;
            }


            public void initRelColumn(List<Map.Entry<String, String>> relColumn) {
                Matcher aKey;
                Matcher bKey;
                IColumn aColumn;
                IColumn bColumn;
                for (Map.Entry<String, String> entry : relColumn) {
                    aKey = DataAccessObjectInfo.COLUMN.matcher(entry.getKey());
                    bKey = DataAccessObjectInfo.COLUMN.matcher(entry.getValue());
                    boolean a = aKey.find() && bKey.find();
                    aColumn = getColumn(aKey.group(1), aKey.group(2));
                    bColumn = getColumn(bKey.group(1), bKey.group(2));
//                    System.out.println(aKey.group(1) + "." + aKey.group(2));
//                    System.out.println(bKey.group(1) + "." + bKey.group(2));
//                    if (aColumn == null || bColumn == null) {
//                        throw new NullPointerException();
//                    }
                    aColumn.linkColumn(bColumn);
                    bColumn.linkColumn(aColumn);
                }

            }

            public class Column implements IColumn {
                final Table.Column column;
                final Set<IColumn> linkColumns = new HashSet<>();

                public Column(Table.Column column) {
                    this.column = column;
                }


                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass().isAssignableFrom(o.getClass()) || o.getClass().isAssignableFrom(getClass()))
                        return false;
                    if (o instanceof Column) {
                        return Objects.equals(column, ((Column) o).column);
                    } else {
                        return o.equals(column);
                    }
                }

                @Override
                public int hashCode() {
                    return column != null ? column.hashCode() : 0;
                }

                @Override
                public void linkColumn(IColumn column) {
                    linkColumns.add(column);
                }

                public GroupTable getTable() {
                    return GroupTable.this;
                }
            }
        }

        public class Table implements ITable {
            final String name;
            final String alias;
            final Column[] columns;
            final String[] filter;
            final TableRel rel;
            ITable parent;

            public Table(String name, String alias, ColumnArg[] columnArgs, String[] filter, TableRel rel) {
                this.name = name;
                this.alias = alias;
                this.filter = filter;
                this.rel = rel;
                this.columns = Arrays.stream(columnArgs).map(columnArg -> this.new Column(columnArg)).toArray(Column[]::new);
            }

            public void setParent(ITable parent) {
                this.parent = parent;
            }

            public IColumn getColumn(String key) {
                for (Column column : columns) {
                    if (column.name.equals(key)) return column;
                }
                return null;
            }

            @Override
            public TableRel getRel() {
                return rel;
            }

            @Override
            public boolean match(String key) {
                return alias.equals(key);
            }

            @Override
            public SqlBuilder build(Map<String, Object> map, boolean start) {
                return null;
            }

            @Override
            public IColumn getColumn(String tableKey, String columnKey) {
                if (tableKey.equals(alias)) {
                    return getColumn(columnKey);
                }
                return null;
            }

            @Override
            public IColumn getAliasColumn(String alias) {
                for (Column column : columns) {
                    if (alias.equals(column.alias)) return column;
                }
                return null;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass().isAssignableFrom(o.getClass()) || o.getClass().isAssignableFrom(getClass()))
                    return false;
                Table table = (Table) o;
                return Objects.equals(alias, table.alias);
            }

            @Override
            public int hashCode() {
                return alias != null ? alias.hashCode() : 0;
            }


            public class Column extends ColumnArg implements IColumn {
                Set<IColumn> linkColumns = new HashSet<>();

                public Column(ColumnArg arg) {
                    super(arg.name, arg.alias);
                }

                public Table getTable() {
                    return Table.this;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass().isAssignableFrom(o.getClass()) || o.getClass().isAssignableFrom(getClass()))
                        return false;
                    if (o instanceof Column) {
                        Column column = (Column) o;
                        return Objects.equals(this.name, column.name) && Objects.equals(getTable(), column.getTable());
                    } else {
                        return o.equals(this);
                    }
                }

                @Override
                public int hashCode() {
                    int result = name != null ? name.hashCode() : 0;
                    result = 31 * result + (getTable() != null ? getTable().hashCode() : 0);
                    return result;
                }

                public void linkColumn(IColumn column) {
                    if (this.equals(column)) return;
                    this.linkColumns.add(column);
                }
            }

        }
    }

}
