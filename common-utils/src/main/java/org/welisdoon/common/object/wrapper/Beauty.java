package org.welisdoon.common.object.wrapper;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.data.BaseCondition;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname Beauty
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class Beauty {
    final Set<RelCol> relCols = new HashSet<>();
    final Set<RelTab> relTabs = new HashSet<>();
    final SqlMapper.MainTable mainTable;
    final static String
            COLUMN = "{column}",
            TABLE = "{table}",
            WHERE = "{where}",
            EQUAL = " {0} = {1} ",
            MARK_1 = "/*@@##*/",
            MARK_2 = "/*##@@*/",
            EQUAL_PARAM = MessageFormat.format("{0} {1} {2}", MARK_1, "{0} = #'{'{1}'}'", MARK_2),
            JOIN = "{JOIN}",
            AND = " and ",
            COLUMN_AS = " {0} AS \"{1}\" ",
            JOIN_ON = MessageFormat.format("JOIN {0} ON {1} {2}", TABLE, WHERE, JOIN),
            BODY = MessageFormat.format("select {0} from {1} {2} where {3}", COLUMN, TABLE, JOIN, WHERE),
            BODY_EXISTS = MessageFormat.format(" exists ( select 1 from {0} {1} where {2} )", TABLE, JOIN, WHERE);

    final Prepare.MainPart part;

    public Beauty(SqlMapper.MainTable mainTable) {
        this.mainTable = mainTable;
        for (SqlMapper.LinkColumn column1 : mainTable.getColumns()) {
            SqlMapper.BaseColumn baseColumn1 = column1.getLeafColumn();
            if (baseColumn1.relColumn != null) {
                for (SqlMapper.LinkColumn column2 : mainTable.getColumns()) {
                    if (column2 == column1) continue;
                    if (column2.matched(baseColumn1.relColumn.tableAlias, baseColumn1.relColumn.columnName)) {
                        SqlMapper.BaseColumn baseColumn2 = column2.getLeafColumn();
                        relCols.add(new RelCol(baseColumn1, baseColumn2));
                        relTabs.add(new RelTab(baseColumn1.getTable(), baseColumn2.getTable()));
                    }
                }
            }
        }

        part = new Prepare.MainPart(this);
    }

    public List<Object> page(Map<String, Object> params, BaseCondition.Page page) {
        PagePrepare prepare = pagePrepare(params, page);
        return query(prepare);
    }

    public List<Object> query(Prepare prepare) {
        if (SqlMapper.getDataSource(mainTable) == null) {
            return List.of(1, 2);
        }
        List<Object> list = new LinkedList<>();
        prepare.query(SqlMapper.getConnect(mainTable), (resultSetMetaData, resultSet) -> {
            list.add(resultSet.getObject(1));
            return true;
        });
        return list;
    }

    public List<Object> query(Map<String, Object> params) {
        Prepare prepare = prepare(params);
        return query(prepare);
    }

    public PagePrepare pagePrepare(Map<String, Object> params, BaseCondition.Page page) {
        PagePrepare pagePrepare = new PagePrepare(prepare(params), PagePrepare.DefaultPageFormat.getFormat(SqlMapper.getDataSource(mainTable)));
        pagePrepare.log("");
        return pagePrepare;
    }

    public Prepare prepare(Map<String, Object> params) {
        System.out.println(params);
        List<SqlMapper.BaseColumn> list = new LinkedList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            for (SqlMapper.LinkColumn column : mainTable.getColumns()) {
                if (column.getLeafColumn().objectAlias.equals(entry.getKey())) {
                    list.add(column.getLeafColumn());
                }
            }
        }

        AtomicReference<String> selectSql = new AtomicReference<>();
        AtomicReference<String> existsSql = new AtomicReference<>();
        AtomicReference<SqlMapper.BaseTable> firstTable = new AtomicReference<>();
        List<List<SqlMapper.BaseTable>> lists = new LinkedList<>();
        read(mainTable, new AtomicInteger(0), lists, (integer, table) -> {
            switch (integer) {
                case 0:
                    relTabs.forEach(System.out::println);
                    for (SqlMapper.BaseColumn baseColumn : list) {
                        List<List<SqlMapper.BaseTable>> lists1 = new LinkedList<>();
                        System.out.println(table + "======>" + baseColumn.getTable());
                        find(table, baseColumn.getTable(), lists1::add);
//                        lists1.sort(Comparator.comparing(List::size));
//                        lists.add(lists1.get(0));
                        lists.addAll(lists1);
                        relTabs.forEach(relTab -> relTab.use(false));
                    }
                    SqlMapper.BaseColumn keyCol = table.getColumns()[0];
                    selectSql.set(BODY
                            .replace(COLUMN, keyCol.toSql())
                            .replace(TABLE, table.toSql())
                            .replace(JOIN, "")
                            .replace(WHERE, orElse(getCondition(table, list), AND, "") + WHERE)
                    );
                    break;
                case 1:
                    firstTable.set(table);
                    existsSql.set(BODY_EXISTS.replace(TABLE, table.toSql()));
                    break;
                default:
                    existsSql.set(existsSql.get()
                            .replace(JOIN, JOIN_ON
                                    .replace(TABLE, table.toSql())
                                    .replace(WHERE, orElse(getCondition(table, list), "", ""))
                            )
                    );
                    break;
            }
        });
        Prepare prepare = new Prepare(selectSql.get().replace(WHERE, existsSql.get()).replace(WHERE, getCondition(firstTable.get(), list)).replace(JOIN, ""), params);
        return prepare;
    }

    String orElse(String a, String p, String b) {
        if (StringUtils.isNotBlank(a)) return a + p;
        return b;
    }

    protected String getCondition(SqlMapper.BaseTable table, List<SqlMapper.BaseColumn> list) {
        return Stream.of(
                Arrays.stream(table.getColumns()).filter(column -> column.relColumn != null).map(column -> MessageFormat.format(EQUAL, column.toSql(), column.relColumn.toSql())).collect(Collectors.joining(AND)),
                Arrays.stream(table.getColumns()).filter(column -> list.contains(column)).map(column -> MessageFormat.format(EQUAL_PARAM, column.toSql(), column.objectAlias)).collect(Collectors.joining(AND)),
                Arrays.stream(table.filter).collect(Collectors.joining(AND))
        ).filter(StringUtils::isNotBlank).collect(Collectors.joining(AND));
    }

    public void read(SqlMapper.AbstractTable table, AtomicInteger index, List<List<SqlMapper.BaseTable>> matches, BiConsumer<Integer, SqlMapper.BaseTable> consumer) {
        if (uesLess(table, matches)) {
            return;
        }
        if (table instanceof SqlMapper.GroupTable) {
            for (SqlMapper.AbstractTable abstractTable : ((SqlMapper.GroupTable) table).tables) {
                read(abstractTable, index, matches, consumer);
            }
        } else if (table instanceof SqlMapper.BaseTable) {
            consumer.accept(index.getAndIncrement(), (SqlMapper.BaseTable) table);
        }
    }

    public boolean uesLess(SqlMapper.AbstractTable table, List<List<SqlMapper.BaseTable>> matches) {
        return (table.getRel() != IDataAccessObject.TableRel.Strong && Arrays.stream(table.getColumns()).map(abstractColumn -> abstractColumn.getLeafColumn().getTable()).noneMatch(table1 -> matches.stream().anyMatch(baseTables -> baseTables.contains(table1))));
    }

    protected void find(SqlMapper.BaseTable current, SqlMapper.BaseTable target, Consumer<List<SqlMapper.BaseTable>> consumer) {
        if (current == target) {
            List<SqlMapper.BaseTable> list = new LinkedList<>();
            list.add(current);
            consumer.accept(list);
            return;
        }
        for (RelTab relTab : relTabs) {
            if (relTab.isUsing()) continue;
            SqlMapper.BaseTable table = relTab.find(current);
            if (table == null) continue;
            System.out.println(current + "=get=" + table + "                               " + (target == table));
            relTab.use(true);
            if (table == target) {
                List<SqlMapper.BaseTable> list = new LinkedList<>();
                list.add(table);
                list.add(current);
                consumer.accept(list);
                relTab.use(false);
                continue;
            }
            find(table, target, baseTables -> {
                baseTables.add(current);
                consumer.accept(baseTables);
            });
            relTab.use(false);
        }
    }

    public Map<String, Prepare.Result> query(Object id) {
        SqlMapper.BaseColumn keyCol = getMainBaseTable().getColumns()[0];

        Prepare.Parameter parameter = new Prepare.Parameter();
        part.paramLocal.put(parameter, Map.of(keyCol.objectAlias, TypeUtils.cast(id, keyCol.dataType, ParserConfig.getGlobalInstance())));
        part.queries(parameter);
        System.out.println(part);
        System.out.println(parameter.values);
        return parameter.values;
    }

    public LazyValues queryLazy(Object id) {
        SqlMapper.BaseColumn keyCol = getMainBaseTable().getColumns()[0];

        Prepare.Parameter parameter = new Prepare.Parameter();
        part.paramLocal.put(parameter, Map.of(keyCol.objectAlias, TypeUtils.cast(id, keyCol.dataType, ParserConfig.getGlobalInstance())));

        part.queries(parameter, false);
        return new LazyValues(part, parameter);
    }

    public static class LazyValues implements Prepare.Result {

        final Prepare.MainPart part;
        final Prepare.Parameter parameter;

        public LazyValues(Prepare.MainPart part, Prepare.Parameter parameter) {
            this.part = part;
            this.parameter = parameter;
        }

        @Override
        public void findLowerValue(String key, Consumer<Prepare.Result> val) {
            parameter.values.findLowerValue(key, val);
        }

        @Override
        public Object getVal() {
            return parameter.values;
        }

        public Prepare.Result getVal(String key) {
            return getVal(part, key);
        }

        protected Prepare.Result getVal(Prepare.Part part, String key) {
            AtomicReference<Prepare.Result> resultAtomicReference = new AtomicReference<>();
            findLowerValue(key, resultAtomicReference::set);
            if (resultAtomicReference.get() != null)
                return resultAtomicReference.get();
            for (SqlMapper.BaseTable table : part.tables) {
                for (SqlMapper.BaseColumn column : table.columns) {
                    if (column.objectAlias.equals(key)) {
                        part.queries(parameter, false);
                    }
                }
            }
            findLowerValue(key, resultAtomicReference::set);
            return resultAtomicReference.get();
        }

        protected void reset() {
            for (String s : new ArrayList<>(parameter.values.keySet())) {
                Prepare.Result result = parameter.values.get(s);
                if (result instanceof Prepare.Value && ((Prepare.Value) result).index == 1) {
                    continue;
                }
                parameter.values.remove(s);
            }
            parameter.loaded.removeIf(part1 -> !(part1 instanceof Prepare.MainPart));
        }
    }


    SqlMapper.BaseTable getMainBaseTable() {
        SqlMapper.AbstractTable main = mainTable;
        do {
            main = ((SqlMapper.GroupTable) main).tables[0];
        }
        while (main instanceof SqlMapper.GroupTable);
        return (SqlMapper.BaseTable) main;
    }


    public abstract static class Ends<T> {
        final T tA;
        final T tB;
        boolean using = false;

        protected Ends(T tA, T tB) {
            boolean revere = compareTo(tA, tB) > 0;
            this.tA = revere ? tB : tA;
            this.tB = revere ? tA : tB;
        }

        public <T extends Ends> T use(boolean using) {
            this.using = using;
            return (T) this;
        }

        public boolean isUsing() {
            return using;
        }

        public T find(T t) {
            return t == tA ? tB : t == tB ? tA : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ends<?> ends = (Ends<?>) o;
            return Objects.equals(tA, ends.tA) && Objects.equals(tB, ends.tB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tA, tB);
        }

        @Override
        public String toString() {
            return tA + "=" + tB;
        }

        public abstract int compareTo(T ta, T tb);
    }

    public static class RelCol extends Ends<SqlMapper.BaseColumn> {
        public RelCol(SqlMapper.BaseColumn colA, SqlMapper.BaseColumn colB) {
            super(colA, colA);
        }

        @Override
        public int compareTo(SqlMapper.BaseColumn ta, SqlMapper.BaseColumn tb) {
            return ta.getTable().tableAlias.compareTo(tb.getTable().tableAlias);
        }
    }

    public static class RelTab extends Ends<SqlMapper.BaseTable> {

        protected RelTab(SqlMapper.BaseTable tA, SqlMapper.BaseTable tB) {
            super(tA, tB);

        }

        @Override
        public int compareTo(SqlMapper.BaseTable ta, SqlMapper.BaseTable tb) {
            return ta.tableAlias.compareTo(tb.tableAlias);
        }

    }
}
