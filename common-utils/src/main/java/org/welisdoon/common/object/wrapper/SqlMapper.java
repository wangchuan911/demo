package org.welisdoon.common.object.wrapper;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.MyBatisUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname SqlMapper
 * @Description TODO
 * @Author Septem
 * @Date 18:32
 */
public class SqlMapper {

    static class Prepare {
        final String sql;
        final List<Object> params;

        Prepare(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }

        Prepare(String prepareSql, Map<String, Object> params) {
            this.params = new LinkedList<>();
            MyBatisUtils.readSqlTemplate(prepareSql, (name, jdbcType) -> {
                Object value = params.get(name);
                if (value == null || StringUtils.isEmpty(value.toString())) {
                    this.params.add(null);
                } else if (jdbcType != null) {
                    try {
                        value = MyBatisUtils.getValue(jdbcType, value);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("属性[" + name + "=" + value + "]转sql参数失败：" + e.getMessage(), e);
                    }
                }
                this.params.add(value);
            });
            this.sql = prepareSql.replaceAll(MyBatisUtils.PATTERN_STRING, "?");
        }
    }

    static class PartSelect {
        String sql;
    }

    public Prepare prepare;
//    protected List<Map.Entry<String, String>> relColumn = new LinkedList<>();


//    protected void init(GroupTable table) {
//        table.initRelColumn(relColumn);
//        relColumn.clear();
//    }

    public void build(MainTable table, Map<String, Object> params) {
        BuildOption option = new BuildOption(Mode.Params);
        option.params = params;
        table.setOption(option);
        table.build();
        prepare = new Prepare(String.format("select %s %s", table.option.part.columns, table.option.part.tables), params);
    }

    public void build(MainTable table, Object id) {
        BuildOption option = new BuildOption(Mode.Key);
        table.setOption(option);
        table.build();
//        prepare = new Prepare(String.format("select %s %s", table.option.part.columns, table.option.part.tables), id);
    }

    public static class ColumnArg {
        String columnName;
        String objectAlias;
        String linkColumn;

        public ColumnArg(String columnName, String objectAlias, String linkColumn) {
            this.columnName = columnName;
            this.objectAlias = objectAlias;
            this.linkColumn = linkColumn;
        }
    }

/*    public static class SqlBuilder {
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        List<Object> params = new LinkedList<>();

        public SqlBuilder(SqlBuilder... sqlBuilders) {
//                    if (sqlBuilders == null || sqlBuilders.length == 0) return;
//                    StringBuilder from2 = new StringBuilder();
//                    for (int i = 0; i < sqlBuilders.length; i++) {
//                        append(select, sqlBuilders[i].select, ",");
//                        if (i != 0) {
//                            append(from2, sqlBuilders[i].from, " ");
//                            params.addAll(sqlBuilders[i].params);
//                        }
//                    }
//                    this.from.append(sqlBuilders[0].format(sqlBuilders[0].params, from2.toString()));
            this(Arrays.asList(sqlBuilders));
        }

        public SqlBuilder(List<SqlBuilder> sqlBuilders) {
            if (sqlBuilders == null || sqlBuilders.isEmpty()) return;
            StringBuilder from2 = new StringBuilder();
            for (int i = 0; i < sqlBuilders.size(); i++) {
                SqlBuilder sqlBuilder = sqlBuilders.get(i);
                append(select, sqlBuilder.select, ",");
                if (i != 0) {
                    append(from2, sqlBuilder.from, " ");
                    params.addAll(sqlBuilder.params);
                }
            }
            this.from.append(sqlBuilders.get(0).format(sqlBuilders.get(0).params, from2.toString()));
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
            String sql = this.from.toString().replace(BaseTable.JOIN_CUT_POINT, join);
            this.params.addAll(params);
            return sql;
        }
    }*/


    public static class BuildOption {
        Format format;
        boolean use;
        Map<String, Object> params;
        SqlPart part;
        PartSelect select;
        final Mode mode;

        public BuildOption(Mode mode) {
            this.mode = mode;
        }

        public static class SqlPart {
            final String tables;
            final String columns;

            public SqlPart(String tables, String columns) {
                this.tables = tables;
                this.columns = columns;
            }

            @Override
            public String toString() {
                return "Part[" +
                        "tables='" + tables + '\'' +
                        ", columns='" + columns + '\'' +
                        ']';
            }
        }

    }

    enum Mode {
        Params, Key
    }

    public enum Format {
        From, Join, Unknown;
    }

    public abstract static class AbstractTable<C extends AbstractTable.AbstractColumn> {

        final IDataAccessObject.TableRel rel;
        AbstractTable parent;
        C[] columns;
        BuildOption option;
        int level = 0;

        protected AbstractTable(IDataAccessObject.TableRel rel) {
            this.rel = rel;
        }

        abstract Map<String, Object> filter(Map<String, Object> params);

        abstract boolean match(String key);

        public void setOption(BuildOption option) {
            this.option = option;
        }

        /*protected void updateOption() {
            if (MapUtils.isNotEmpty(option.params)) {
                for (C column : this.columns) {
                    for (AbstractColumn linkColumn : (List<AbstractTable.AbstractColumn<?>>) column.linkColumns) {
                        setOptionUse(linkColumn.getTable());
                        setOptionUse(this);
                    }
                }
            }
        }

        protected void setOptionUse(AbstractTable table) {
            if (table == null || table.getOption() == null || table.getOption().use) return;
            table.getOption().use = true;
            setOptionUse(table.getParent());
            System.out.println(table + " use");
        }*/


        public C[] getColumns() {
            return columns;
        }

        public Stream<C> getColumnsStream() {
            return Arrays.stream(columns);
        }

        public BuildOption getOption() {
            return option;
        }

        abstract void build();

        public boolean isUse() {
            return getOption().use;
        }

        public abstract static class AbstractColumn<T extends AbstractTable> {
            final T table;
            AbstractColumn<?> parent;
//            List<AbstractTable.AbstractColumn<?>> linkColumns = new LinkedList<>();

            abstract protected <T extends AbstractColumn<?>> T getLeafColumn();

            public AbstractColumn(T table) {
                this.table = table;
            }

            final public T getTable() {
                return table;
            }

            public void setParent(AbstractColumn<?> parent) {
                this.parent = parent;
            }

            public AbstractColumn<?> getParent() {
                return parent;
            }

//            public boolean linkColumn(AbstractTable.AbstractColumn column) {
//                if (this.linkColumns.contains(column)) return false;
//                this.linkColumns.add(column);
//                return true;
//            }

            abstract boolean matched(String tableAlias, String columnName);
        }

        abstract AbstractColumn getColumn(String tableAlias, String columnKey);

        abstract AbstractColumn getAliasColumn(String alias);

        final public AbstractTable setParent(AbstractTable parent) {
            this.parent = parent;
            this.level = parent.level + 1;
            return this;
        }

        final public AbstractTable getParent() {
            return this.parent;
        }

        final public IDataAccessObject.TableRel getRel() {
            return rel;
        }


    }

    public static class MainTable extends GroupTable {

        public MainTable(String name, AbstractTable[] iTables, IDataAccessObject.TableRel rel) {
            super(name, iTables, rel);
        }
    }

    public static class GroupTable extends AbstractTable<LinkColumn> {
        String name;
        final AbstractTable[] tables;

        public GroupTable(String name, AbstractTable[] iTables, IDataAccessObject.TableRel rel) {
            super(rel);
            this.tables = iTables;
            this.columns = Arrays.stream(iTables).flatMap(iTable -> {
                return Arrays.stream(iTable.columns).map(column -> new LinkColumn(this, column));
            }).toArray(LinkColumn[]::new);
            this.name = name;
            for (AbstractTable iTable : iTables) {
                iTable.setParent(this);
            }
            /*List<AbstractColumn> allColumn = Arrays.stream(this.columns).map(linkColumn -> linkColumn.column).collect(Collectors.toList());
            List<BaseColumn> baseColumn = allColumn.stream().filter(abstractColumn -> abstractColumn instanceof BaseColumn).map(abstractColumn -> (BaseColumn) abstractColumn).collect(Collectors.toList());
            List<LinkColumn> linkColumn = allColumn.stream().filter(abstractColumn -> abstractColumn instanceof LinkColumn).map(abstractColumn -> (LinkColumn) abstractColumn).collect(Collectors.toList());

            System.out.println("初始化" + this);
            linkColumnLink(baseColumn, linkColumn);
            linkColumnLink(linkColumn, allColumn);
        }

        protected void linkColumnLink(List<? extends AbstractColumn> self, List<? extends AbstractColumn> all) {
            boolean find;
            for (AbstractColumn columnA : self) {
                find = false;
                if (!matched(columnA)) continue;
                for (AbstractColumn columnA1 : self) {
                    if (columnA == columnA1) continue;
                    if (((BaseColumn) columnA.getLeafColumn()).relColumn.matched(((BaseColumn) columnA1.getLeafColumn()))) {
                        linkColumn(columnA, columnA1);
                        find = true;
                        break;
                    }
                }
                if (find) {
                    continue;
                }
                for (AbstractColumn columnC : all) {
                    if (columnC == columnA) continue;
                    if (!columnC.matched(((BaseColumn) columnA.getLeafColumn()).relColumn.tableAlias, ((BaseColumn) columnA.getLeafColumn()).relColumn.columnName))
                        continue;
                    linkColumn(columnA, columnC);
                    continue;
                }
            }
        }

        protected boolean matched(AbstractColumn abstractColumn) {
            if (abstractColumn == null) return false;
            BaseColumn baseColumn = (BaseColumn) abstractColumn.getLeafColumn();
            if (baseColumn == null || baseColumn.relColumn == null) return false;
            return true;
        }

        public void linkColumn(AbstractColumn columnA, AbstractColumn columnB) {
            if (columnA == null || columnB == null) return;
            boolean c;
            columnA.linkColumn(columnB);
            c = columnB.linkColumn(columnA);
            if (c) System.out.println("绑定->" + columnA + "---" + columnB);*/
        }


        @Override
        public String toString() {
            return "GT[" + name + ']';
        }

        @Override
        public Map<String, Object> filter(Map<String, Object> params) {
            Map<String, Object> map = new HashMap<>();
            for (AbstractTable table : this.tables) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (table.getAliasColumn(entry.getKey()) == null) continue;
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            return map;
        }

        @Override
        public boolean match(String key) {
            for (AbstractTable table : tables) {
                if (table.match(key)) return true;
            }
            return false;
        }

        @Override
        public void setOption(BuildOption option) {
            super.setOption(option);
            for (AbstractTable table : this.tables) {
                BuildOption option1 = new BuildOption(option.mode);
                option1.params = table.filter(option.params);
                table.setOption(option1);
            }
        }

        @Override
        public BuildOption getOption() {
            return option;
        }

        @Override
        public void build() {
            Mode mode = getOption().mode;
            switch (mode) {
                case Params:
                    AbstractTable[] tables = Arrays.stream(this.tables).filter(iTable -> useFul(iTable)).toArray(AbstractTable[]::new);
                    List<BuildOption.SqlPart> join = new LinkedList<>();
                    List<String> exists = new LinkedList<>();
                    AbstractTable table = tables[0];
                    table.getOption().format = Format.From;
                    table.build();
                    BuildOption.SqlPart part = table.option.part;
                    if (part == null)
                        throw new IllegalStateException(this.toString() + " build sql syntax error ");
                    for (int i = 1; i < tables.length; i++) {
                        table = tables[i];
                        table.getOption().format = Format.Join;
                        table.build();
                        if (table instanceof BaseTable) {
                            join.add(table.option.part);
                        } else {
                            exists.add(String.format("exists (select 1 %s)", table.option.part.tables));
                        }

                    }
                    this.option.part = new BuildOption.SqlPart(
                            part.tables.replace(BaseTable.JOIN_CUT_POINT, join.stream().map(sqlPart -> sqlPart.tables).collect(Collectors.joining(" "))) + exists.stream().collect(Collectors.joining(" and")),
                            Stream.of(join.stream(), Stream.of(part)).flatMap(stringStream -> stringStream).map(sqlPart -> sqlPart.columns).filter(StringUtils::isNotEmpty).collect(Collectors.joining(","))
                    );
                    System.out.println("build[" + this + "]-->" + this.option.part);
                    break;
                case Key:
                    List<BaseTable> table2 = new LinkedList<>();
                    for (AbstractTable table1 : this.tables) {
                        if (table1 instanceof GroupTable) {
                            table1.build();
                        } else if (table1 instanceof BaseTable) {
                            if (table1.getRel() == IDataAccessObject.TableRel.Multi) {
                                table1.build();
                            } else {
                                table2.add((BaseTable) table1);
                            }
                        }
                    }
                    for (int i = 0; i < table2.size(); i++) {
                        BaseTable baseTable = table2.get(i);
                        baseTable.option.format = i == 0 ? Format.From : Format.Join;
                        baseTable.build();
                    }
            }
        }


        protected boolean useFul(AbstractTable iTable) {
            return iTable != null && (iTable.getOption().use || iTable.getRel() == IDataAccessObject.TableRel.Strong);
        }


        public AbstractColumn getColumn(String tableAlias, String columnKey) {
            for (AbstractTable table : tables) {
                if (table instanceof GroupTable) {
                    AbstractColumn iColumn = table.getColumn(tableAlias, columnKey);
                    for (LinkColumn column : columns) {
                        if (column.equals(iColumn)) {
                            return column;
                        }
                    }
                } else if (table instanceof BaseTable && table.match(tableAlias)) {
                    return ((BaseTable) table).getColumn(columnKey);
                }
            }
            return null;
        }

        @Override
        public AbstractColumn getAliasColumn(String alias) {
            for (LinkColumn column : columns) {
                if (alias.equals(column.getLeafColumn().objectAlias)) return column;
            }
            return null;
        }


//        public void initRelColumn(List<Map.Entry<String, String>> relColumn) {
//            Matcher aKey;
//            Matcher bKey;
//            AbstractColumn aColumn;
//            AbstractColumn bColumn;
//            for (Map.Entry<String, String> entry : relColumn) {
//                aKey = IDataAccessObject.DataAccessObjectInfo.COLUMN.matcher(entry.getKey());
//                bKey = IDataAccessObject.DataAccessObjectInfo.COLUMN.matcher(entry.getValue());
//                boolean a = aKey.find() && bKey.find();
//                aColumn = getColumn(aKey.group(1), aKey.group(2));
//                bColumn = getColumn(bKey.group(1), bKey.group(2));
//                aColumn.linkColumn(bColumn);
//                bColumn.linkColumn(aColumn);
//            }
//        }
    }

    public static class LinkColumn extends AbstractTable.AbstractColumn {
        final AbstractTable.AbstractColumn column;

        public LinkColumn(AbstractTable abstractTable, AbstractTable.AbstractColumn column) {
            super(abstractTable);
            this.column = column;
            column.setParent(this);
        }


        public boolean matched(String tableAlias, String columnName) {
            BaseColumn column = getLeafColumn();
            return column != null && column.matched(tableAlias, columnName);
        }

        public BaseColumn getLeafColumn() {
            if (column instanceof BaseColumn) return (BaseColumn) column;
            if (column instanceof LinkColumn) return ((LinkColumn) column).getLeafColumn();
            return null;
        }

        @Override
        public String toString() {
            return "LC[" + ((GroupTable) getTable()).name + "][" + column + ']';
        }
    }

    public static class BaseTable extends AbstractTable<BaseColumn> {
        final static String JOIN_CUT_POINT = "{{join}}";
        final String tableName;
        final String tableAlias;
        final String[] filter;

        public BaseTable(String tableName, String alias, List<ColumnArg> columnArgs, String[] filter, IDataAccessObject.TableRel rel) {
            super(rel);
            this.columns = columnArgs.stream().map(columnArg -> new BaseColumn(this, columnArg)).toArray(BaseColumn[]::new);
            this.tableName = tableName;
            this.tableAlias = alias;
            this.filter = filter;
        }


        public BaseColumn getColumn(String key) {
            for (BaseColumn column : columns) {
                if (column.columnName.equals(key)) return column;
            }
            return null;
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
            return tableAlias.equals(key);
        }

        @Override
        public BuildOption getOption() {
            return option;
        }

        @Override
        public void build() {
            //弱关联 不做处理
            Mode mode = getOption().mode;
            switch (mode) {
                case Params:
                    switch (option.format) {
                        case From:
                            this.option.part = new BuildOption.SqlPart(
                                    String.format(
                                            "from %s %s %s where %s",
                                            this.tableName,
                                            this.tableAlias,
                                            JOIN_CUT_POINT,
                                            buildCondition(option.params)),
                                    Arrays.stream(getColumns()).filter(column -> column.relColumn != null || (column.getTable().tableAlias.equalsIgnoreCase("T1") && getColumns()[0] == column)).map(BaseColumn::toSql).collect(Collectors.joining(",")));
                            break;
                        case Join:
                            this.option.part = new BuildOption.SqlPart(
                                    String.format(
                                            "join %s %s on %s",
                                            this.tableName,
                                            this.tableAlias,
                                            buildCondition(option.params)),
                                    Arrays.stream(getColumns()).filter(column -> column.relColumn != null).map(BaseColumn::toSql).collect(Collectors.joining(",")));
                        default:
                            break;
                    }
                    System.out.println("build[" + this + "]-->" + this.option.part);
                    break;
            }
        }


        protected String buildCondition(Map<String, Object> map) {
            return Stream.of(
                    map.entrySet().stream().map(entry -> {
                        BaseColumn column = this.getAliasColumn(entry.getKey());
                        return String.format("%s = #{%s}", column.toSql(), entry.getKey());
                    }),
                    Arrays.stream(filter),
                    Arrays.stream(columns).filter(baseColumn -> baseColumn.relColumn != null).map(baseColumn -> String.format("%s = %s", baseColumn.relColumn.toSql(), baseColumn.toSql()))
            ).flatMap(stringStream -> stringStream).collect(Collectors.joining(" and "));
        }

        @Override
        public BaseColumn getColumn(String tableAlias, String columnKey) {
            if (tableAlias.equals(this.tableAlias)) {
                return getColumn(columnKey);
            }
            return null;
        }

        @Override
        public BaseColumn getAliasColumn(String alias) {
            for (BaseColumn column : columns) {
                if (alias.equals(column.objectAlias)) return column;
            }
            return null;
        }

        @Override
        public String toString() {
            return "BT[" + tableAlias + "." + tableName + ']';
        }

        @Override
        public void setOption(BuildOption option) {
            super.setOption(option);
            if (option.params.isEmpty()) return;
            for (BaseColumn column : columns) {
                if (column.relColumn == null) continue;
                System.out.println(option.params.keySet());
                System.out.println(column.objectAlias);
                BaseColumn relBaseColumn = BaseColumn.findRel(this, column.relColumn);
                if (relBaseColumn != null) setOptionUse(relBaseColumn.getTable());
                setOptionUse(this);
            }
        }

        protected void setOptionUse(AbstractTable table) {
            if (table == null || table.getOption() == null || table.getOption().use) return;
            table.getOption().use = true;
            setOptionUse(table.getParent());
            System.out.println(table + " use");
        }
    }

    public static class BaseColumn extends AbstractTable.AbstractColumn<BaseTable> {
        String columnName;
        String objectAlias;
        RelColumnInfo relColumn;

        public BaseColumn(BaseTable table, ColumnArg arg) {
            super(table);
            this.columnName = arg.columnName;
            this.objectAlias = arg.objectAlias;
            Matcher aKey;
            if (StringUtils.isNotEmpty(arg.linkColumn) && (aKey = IDataAccessObject.ObjectScanner.COLUMN.matcher(arg.linkColumn)).find()) {
                this.relColumn = new RelColumnInfo(aKey.group(1), aKey.group(2));
            }
        }

        protected String toSql() {
            return String.format("%s.%s", this.getTable().tableAlias, this.columnName);
        }

        @Override
        protected BaseColumn getLeafColumn() {
            return this;
        }

        /*@Override
        public void linkColumn(AbstractTable.AbstractColumn column) {
            if (relColumn != null) {
                BaseColumn column1;
                if (column instanceof LinkColumn) {
                    column1 = ((LinkColumn) column).getLeafColumn();
                } else {
                    column1 = (BaseColumn) column;
                }
                if (relColumn.matched(column1)) {
                    relColumn = null;
                }
            }
            super.linkColumn(column);
        }*/

        @Override
        boolean matched(String tableName, String columnName) {
            return Objects.equals(getTable().tableAlias, tableName) && Objects.equals(columnName, this.columnName);
        }

        public static class RelColumnInfo {
            final String columnName;
            final String tableAlias;


            public RelColumnInfo(String tableAlias, String columnName) {
                this.columnName = columnName;
                this.tableAlias = tableAlias;
            }

            boolean matched(BaseColumn column1) {
                return column1 != null && this.tableAlias.equals(column1.getTable().tableAlias) && this.columnName.equals(column1.columnName);
            }

            public String toSql() {
                return String.format("%s.%s", this.tableAlias, this.columnName);
            }
        }

        static BaseColumn findRel(AbstractTable scanTable, RelColumnInfo target) {
            return findRel(scanTable, target, null);
        }

        static BaseColumn findRel(AbstractTable scanTable, final RelColumnInfo target, AbstractTable exclude) {
            if (scanTable == null) {
                return null;
            }
            for (AbstractTable.AbstractColumn baseColumn : scanTable.columns) {
                if (baseColumn instanceof LinkColumn && ((LinkColumn) baseColumn).column.getTable() == exclude) {
                    continue;
                }
                if (target.matched((BaseColumn) baseColumn.getLeafColumn())) {
                    return (BaseColumn) baseColumn.getLeafColumn();
                }
            }
            return findRel(scanTable.getParent(), target, scanTable);
        }

        @Override
        public String toString() {
            return "BC[" + getTable().tableAlias + "." + columnName + "]";
        }
    }
}
