package org.welisdoon.common.object.wrapper;

import org.apache.commons.lang3.StringUtils;

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
    final AtomicInteger index = new AtomicInteger();
    final static String
            COLUMN = "{column}",
            TABLE = "{table}",
            WHERE = "{where}",
            JOIN = "{JOIN}",
            AND = " and ",
            JION_ON = MessageFormat.format("JOIN {0} ON {1} {2}", TABLE, WHERE, JOIN),
            BODY = MessageFormat.format("select {0} from {1} {2} where {3}", COLUMN, TABLE, JOIN, WHERE),
            BODY_EXISTS = MessageFormat.format(" exists ( select 1 from {0} {1} where {2} )", TABLE, JOIN, WHERE);

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
                            .replace(JOIN, JION_ON
                                    .replace(TABLE, table.toSql())
                                    .replace(WHERE, orElse(getCondition(table, list), "", ""))
                            )
                    );
                    break;
            }
        });
        return new Prepare(selectSql.get().replace(WHERE, existsSql.get()).replace(WHERE, getCondition(firstTable.get(), list)).replace(JOIN, ""), params);
    }

    String orElse(String a, String p, String b) {
        if (StringUtils.isNotBlank(a)) return a + p;
        return b;
    }

    protected String getCondition(SqlMapper.BaseTable table, List<SqlMapper.BaseColumn> list) {
        return Stream.of(
                Arrays.stream(table.getColumns()).filter(column -> column.relColumn != null).map(column -> column.toSql() + " = " + column.relColumn.toSql()).collect(Collectors.joining(AND)),
                Arrays.stream(table.getColumns()).filter(column -> list.contains(column)).map(column -> column.toSql() + " = ${" + column.objectAlias + "}").collect(Collectors.joining(AND)),
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

    public void prepare(Object id) {
        SqlMapper.BaseColumn keyCol = getMainBaseTable().getColumns()[0];
        Prepare.Part part = new Prepare.SinglePart(index.getAndIncrement(), IDataAccessObject.TableRel.Strong);
        find(part, mainTable);
        part.merge();
        System.out.println(part);
    }

    protected void find(Prepare.Part<?> part, SqlMapper.AbstractTable<?> table) {
        if (table instanceof SqlMapper.GroupTable) {
            for (int i = 0; i < ((SqlMapper.GroupTable) table).tables.length; i++) {
                if (i == 0 && table.rel != IDataAccessObject.TableRel.Strong) {
                    Prepare.Part<?> part1 = newPart(table.rel);
                    part.add(part1);
                    part = part1;
                }
                SqlMapper.AbstractTable<?> abstractTable = ((SqlMapper.GroupTable) table).tables[i];
                if (abstractTable instanceof SqlMapper.GroupTable) {
                    find(part, abstractTable);
                    continue;
                }
                if (abstractTable.rel == IDataAccessObject.TableRel.Strong) {
                    part.add((SqlMapper.BaseTable) abstractTable);
                } else {
                    part.add(newPart(abstractTable.getRel()).add((SqlMapper.BaseTable) abstractTable));
                }
            }
        } else {
            part.add((SqlMapper.BaseTable) table);
        }
    }

    Prepare.Part newPart(IDataAccessObject.TableRel rel) {
        return rel == IDataAccessObject.TableRel.Multi ? new Prepare.MultiPart(index.getAndIncrement(), rel) : new Prepare.SinglePart(index.getAndIncrement(), rel);
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
