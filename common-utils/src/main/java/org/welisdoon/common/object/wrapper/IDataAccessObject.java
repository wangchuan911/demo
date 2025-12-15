package org.welisdoon.common.object.wrapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.welisdoon.common.data.BaseCondition;

import java.lang.annotation.*;
import java.lang.reflect.Method;
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
        ITable.SqlBuilder sqlBuilder;
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
            sqlBuilder = table.build(params, new ITable.SqlBuilder.Option().setFormat(ITable.SqlBuilder.Format.From));
        }

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
                StringBuilder select = new StringBuilder();
                StringBuilder from = new StringBuilder();
                List<Object> params = new LinkedList<>();

                public SqlBuilder(SqlBuilder... sqlBuilders) {
                    if (sqlBuilders == null || sqlBuilders.length == 0) return;
                    StringBuilder from2 = new StringBuilder();
                    for (int i = 0; i < sqlBuilders.length; i++) {
                        append(select, sqlBuilders[i].select, ",");
                        if (i != 0) {
                            append(from2, sqlBuilders[i].from, " ");
                            params.addAll(sqlBuilders[i].params);
                        }
                    }
                    this.from.append(sqlBuilders[0].format(sqlBuilders[0].params, from2.toString()));
                }

                protected void append(StringBuilder s, StringBuilder s1, String split) {
                    if (s.length() == 0) {
                        s.append(s1);
                        return;
                    }
                    s.append(split).append(s1);
                }

                public String format(List<Object> params) {
                    return this.format(params, "");
                }

                public String format(List<Object> params, String join) {
                    String sql = this.from.toString().replace(Table.JOIN_CUT_POINT, join);
                    this.params.addAll(params);
                    return sql;
                }

                public static class Option {
                    Format format;
                    final Option parent;

                    public Option() {
                        this(null);
                    }

                    public Option(Option parent) {
                        this.parent = parent;
                    }

                    public Option setFormat(Format format) {
                        this.format = format;
                        return this;
                    }

                    public Option childOption() {
                        return new Option(this);
                    }

                    boolean isUse(ITable iTable) {
                        return false;
                    }
                }


                public enum Format {
                    From, Join;
                }
            }

            Map<String, Object> filter(Map<String, Object> params);

            boolean match(String key);

            SqlBuilder build(Map<String, Object> map, SqlBuilder.Option option);

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

            @Override
            public Map<String, Object> filter(Map<String, Object> params) {
                Map<String, Object> map = new HashMap<>();
                for (ITable table : this.tables) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (table.getAliasColumn(entry.getKey()) == null) continue;
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
                return map;
            }

            @Override
            public boolean match(String key) {
                for (ITable table : tables) {
                    if (table.match(key)) return true;
                }
                return false;
            }

            @Override
            public SqlBuilder build(Map<String, Object> params, SqlBuilder.Option option) {
                SqlBuilder sqlBuilder;
                if (params.isEmpty()) {
                    AtomicInteger index = new AtomicInteger(0);
                    sqlBuilder = new ITable.SqlBuilder(Arrays.stream(this.tables).map(iTable -> iTable.build(params, option.childOption().setFormat(index.getAndIncrement() == 0 ? ITable.SqlBuilder.Format.From : ITable.SqlBuilder.Format.Join))).toArray(ITable.SqlBuilder[]::new));
                } else {
                    sqlBuilder = this.build(params, new ITable.SqlBuilder.Option().setFormat(ITable.SqlBuilder.Format.From));
                }
                return sqlBuilder;
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
            final static String JOIN_CUT_POINT = "{{join}}";
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

            public Column getColumn(String key) {
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
            public Map<String, Object> filter(Map<String, Object> params) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (this.getAliasColumn(entry.getKey()) == null) continue;
                    map.put(entry.getKey(), entry.getValue());
                }
                return map;
            }

            @Override
            public boolean match(String key) {
                return alias.equals(key);
            }

            @Override
            public SqlBuilder build(Map<String, Object> map, SqlBuilder.Option option) {
                SqlBuilder sqlBuilder = new SqlBuilder();
                //弱关联 不做处理
                if (map.isEmpty() && (rel == TableRel.Weak || rel == TableRel.Multi) && !option.isUse(this)) {
                    return sqlBuilder;
                }
                switch (option.format) {
                    case From:
                        sqlBuilder.from.append(String.format(
                                " from %s %s %S where %s",
                                this.name,
                                this.alias,
                                JOIN_CUT_POINT,
                                buildCondition(map)
                        ));
                        break;
                    case Join:
                        sqlBuilder.from.append(String.format(
                                " join %s %s on %s",
                                this.name,
                                this.alias,
                                buildCondition(map)
                        ));
                        break;
                    default:
                        break;
                }
                return sqlBuilder;
            }

            protected String buildCondition(Map<String, Object> map) {
                return Stream.of(
                        map.entrySet().stream().map(entry -> {
                            Column column = this.getAliasColumn(entry.getKey());
                            sqlBuilder.params.add(entry.getValue());
                            return String.format("%s.%s = ?", this.name, column.name);
                        }),
                        Arrays.stream(filter)
                ).flatMap(stringStream -> stringStream).collect(Collectors.joining(" and "));
            }

            @Override
            public Column getColumn(String tableKey, String columnKey) {
                if (tableKey.equals(alias)) {
                    return getColumn(columnKey);
                }
                return null;
            }

            @Override
            public Column getAliasColumn(String alias) {
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
