package org.welisdoon.common.object.wrapper;

import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @Classname SqlMapper
 * @Description TODO
 * @Author Septem
 * @Date 18:32
 */
public class SqlMapper {
    public static final Map<String, DataSource> DATA_SOURCE_MAP = new HashMap<>();
    final Beauty beauty;

    public SqlMapper(IDataAccessObject.ObjectScanner objectScanner) {
        SqlMapper.MainTable table = objectScanner.sql(this);
        beauty = new Beauty(table);
    }

    public static class ColumnArg {
        String columnName;
        String objectAlias;
        Class<?> dataType;
        String linkColumn;

        public ColumnArg(String columnName, String objectAlias, String linkColumn, Class<?> dataType) {
            this.columnName = columnName;
            this.objectAlias = objectAlias;
            this.linkColumn = linkColumn;
            this.dataType = dataType;
        }
    }

    public Beauty getBeauty() {
        return beauty;
    }

    public static DataSource getDataSource(SqlMapper.AbstractTable<?> table1) {
        SqlMapper.AbstractTable<?> table = table1;
        while (table instanceof SqlMapper.GroupTable) {
            table = ((SqlMapper.GroupTable) table).tables[0];
        }
        return getDataSource(((SqlMapper.BaseTable) table).tableName.split("\\s+")[0]);
    }

    public static DataSource getDataSource(String name) {
        return DATA_SOURCE_MAP.get(name);
    }

    public static Connection getConnect(String name) {
        try {
            return getDataSource(name).getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Connection getConnect(AbstractTable<?> table) {
        try {
            return SqlMapper.getDataSource(table).getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
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
        int level = 0;

        protected AbstractTable(IDataAccessObject.TableRel rel) {
            this.rel = rel;
        }

        abstract boolean match(String key);


        public C[] getColumns() {
            return columns;
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

//        abstract AbstractColumn getAliasColumn(String alias);

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


        }


        @Override
        public String toString() {
            return "GT[" + name + ']';
        }

        @Override
        public boolean match(String key) {
            for (AbstractTable table : tables) {
                if (table.match(key)) return true;
            }
            return false;
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
        public boolean match(String key) {
            return tableAlias.equals(key);
        }

        @Override
        public BaseColumn getColumn(String tableAlias, String columnKey) {
            if (tableAlias.equals(this.tableAlias)) {
                return getColumn(columnKey);
            }
            return null;
        }

        @Override
        public String toString() {
            return "BT[" + tableAlias + "." + tableName + ']';
        }

        public String toSql() {
            return String.format("%s as %s", this.tableName, this.tableAlias);
        }

    }

    public static class BaseColumn extends AbstractTable.AbstractColumn<BaseTable> {
        String columnName;
        String objectAlias;
        Class<?> dataType;
        final RelColumnInfo relColumn;

        public BaseColumn(BaseTable table, ColumnArg arg) {
            super(table);
            this.columnName = arg.columnName;
            this.objectAlias = arg.objectAlias;
            this.dataType = arg.dataType;
            Matcher aKey;
            RelColumnInfo relColumn = null;
            if (StringUtils.isNotEmpty(arg.linkColumn) && (aKey = IDataAccessObject.ObjectScanner.COLUMN.matcher(arg.linkColumn)).find()) {
                relColumn = new RelColumnInfo(aKey.group(1), aKey.group(2));
            }
            this.relColumn = relColumn;
        }

        protected String toSql() {
            return String.format("%s.%s", this.getTable().tableAlias, this.columnName);
        }

        @Override
        protected BaseColumn getLeafColumn() {
            return this;
        }

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

            @Override
            public String toString() {
                return "RelColumnInfo[" + toSql() + ']';
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
