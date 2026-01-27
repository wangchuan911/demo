package org.welisdoon.common.object.wrapper;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.MyBatisUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname Prepare
 * @Description TODO
 * @Author Septem
 * @Date 8:48
 */
public class Prepare {
    final public String sql;
    final public List<Object> params;

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

    public static abstract class Part {
        protected final IDataAccessObject.TableRel rel;
        protected Part parent;
        protected List<SqlMapper.BaseTable> tables = new LinkedList<>();
        protected List<Part> parts = new LinkedList<>();
        protected Map<Part, List<SqlMapper.BaseColumn.RelColumnInfo>> conditionLinks = new HashMap<>();
        final int index;

        public Part(int index, IDataAccessObject.TableRel rel) {
            this.index = index;
            this.rel = rel;
        }


        public Part add(Part part) {
            parts.add(part);
            part.parent = this;
            return this;
        }

        public Part add(SqlMapper.BaseTable table) {
            tables.add(table);
            return this;
        }

        abstract public void load(Parameter parameter);


        public List<SqlMapper.BaseColumn.RelColumnInfo> getOutRelCol() {
            List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos = new LinkedList<>();
            for (SqlMapper.BaseTable table : tables) {
                for (SqlMapper.BaseColumn column : table.getColumns()) {
                    if (column.relColumn != null) {
                        relColumnInfos.add(column.relColumn);
                    }
                }
            }
            filterRelColumn(this, relColumnInfos, null);

            return relColumnInfos;
        }

        protected boolean filterRelColumn(Part part, List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, final Part origin) {
            int i = relColumnInfos.size();
            for (SqlMapper.BaseTable table : part.tables) {
                relColumnInfos.removeIf(relColumn -> {
                    if (table.tableAlias.equals(relColumn.tableAlias)) {
                        if (origin != null) {
                            if (!origin.conditionLinks.containsKey(part))
                                origin.conditionLinks.put(part, new LinkedList<>());
                            origin.conditionLinks.get(part).add(relColumn);
                        }
                        return true;
                    }
                    return false;
                });
            }
            return i > relColumnInfos.size();
        }

        protected void matchedPart(Part part1, List<Part> parts1, List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, final Part origin) {
            for (Part part : parts) {
                filterRelColumn(part, relColumnInfos, origin);
                if (part == part1) break;
                parts1.add(part);
            }
            if (parent != null)
                parent.matchedPart(part1.parent, parts1, relColumnInfos, origin);
            else
                filterRelColumn(this, relColumnInfos, origin);
        }

        protected void getInnerParts(List<Part> parts) {
            for (Part part : this.parts) {
                part.getInnerParts(parts);
                parts.add(part);
            }
        }

        public boolean matchedInPrev(List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, List<Part> parentParts) {
            relColumnInfos.clear();
            relColumnInfos.addAll(this.getOutRelCol());

            parentParts.clear();
            if (relColumnInfos.isEmpty()) return true;
            parent.matchedPart(this, parentParts, relColumnInfos, this);
            return relColumnInfos.isEmpty();
        }

        public List<Part> findInnerParts(List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, List<Part> parentParts) {
            List<Part> parentPartsInner = new LinkedList<>();
            List<Part> matched = new LinkedList<>();
            for (Part parentPart : parentParts) {
                parentPart.getInnerParts(parentPartsInner);
                for (Part part : parentPartsInner) {
                    if (filterRelColumn(part, relColumnInfos, this)) {
                        matched.add(part);
                    }
                }
                parentPartsInner.clear();
            }
            return matched;
        }


        protected Part getRootPart() {
            return parent != null ? parent.getRootPart() : this;
        }

        public void merge() {
            if (parent != null) {
                List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos = new LinkedList<>();
                List<Part> parentParts = new LinkedList<>();
                if (!matchedInPrev(relColumnInfos, parentParts)) {
                    List<Part> inner = findInnerParts(relColumnInfos, parentParts);
                    if (!relColumnInfos.isEmpty())
                        throw new IllegalStateException(relColumnInfos.stream().map(relColumnInfo -> relColumnInfo.toSql()).collect(Collectors.joining(",")) + "没有匹配到对端");
                }
            }
            for (Part part : List.copyOf(parts)) {
                part.merge();
            }
        }

        String toSql() {
            return Beauty.BODY
                    .replace(Beauty.COLUMN, tables.stream().flatMap(table -> Arrays.stream(table.getColumns()).map(column -> MessageFormat.format(Beauty.COLUMN_AS, column.toSql(), column.objectAlias))).collect(Collectors.joining(",")))
                    .replace(Beauty.TABLE, tables.get(0).toSql())
                    .replace(Beauty.JOIN,
                            tables.stream().skip(1)
                                    .map(table -> Beauty.JION_ON
                                            .replace(Beauty.TABLE, table.toSql())
                                            .replace(Beauty.WHERE,
                                                    Stream.of(
                                                            Arrays.stream(table.getColumns()).filter(column -> column.relColumn != null)
                                                                    .map(column -> column.toSql() + " = " + column.relColumn.toSql()),
                                                            Arrays.stream(table.filter)
                                                    )
                                                            .flatMap(stringStream -> stringStream)
                                                            .collect(Collectors.joining(Beauty.AND))))
                                    .collect(Collectors.joining(" ")))
                    .replace(Beauty.WHERE,
                            Stream.of(conditionLinks
                                            .entrySet()
                                            .stream()
                                            .map(entry ->
                                                    entry.getKey().tables.stream()
                                                            .flatMap(table -> Arrays.stream(table.columns)
                                                                    .filter(column -> entry.getValue().stream().anyMatch(relColumnInfo -> column.columnName.equals(relColumnInfo.columnName) && column.getTable().tableAlias.equals(relColumnInfo.tableAlias))))
                                                            .map(column ->
                                                                    MessageFormat.format(Beauty.EQUAL_PARAM,
                                                                            column.toSql(),
                                                                            column.objectAlias
                                                                    )).collect(Collectors.joining(Beauty.AND))

                                            )
                                            .collect(Collectors.joining(Beauty.AND))
                                    , Beauty.WHERE).filter(StringUtils::isNotEmpty).collect(Collectors.joining(Beauty.AND)))
                    .replace(Beauty.JOIN, "");
        }

        @Override
        public String toString() {
            return "Part{" +
                    "rel=" + rel +
                    ", tables=" + tables +
                    ", parts=" + parts +
                    '}';
        }
    }

    public static class MainPart extends Part {

        public MainPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        public void load(Parameter parameter) {
            int i = this.index;
            Part part = this;
            while (!part.parts.isEmpty()) {
                part = parts.get(parts.size() - 1);
                i = Math.max(i, part.index);
            }
            parameter.loaded = new boolean[i + 1];
            System.out.println(toSql().replace(Beauty.WHERE, MessageFormat.format(Beauty.EQUAL_PARAM, tables.get(0).columns[0].toSql(), tables.get(0).columns[0].objectAlias)));
            tables.forEach(table -> Arrays.stream(table.columns).forEach(column -> {
                parameter.values.put(column.objectAlias, new SingleValue(column.objectAlias + "_Val", column.dataType));
            }));
            parameter.loaded[0] = true;
            for (Part part1 : parts) {
                part1.load(parameter);
            }
        }
    }

    public static class OtherPart extends Part {

        public OtherPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        @Override
        public void load(Parameter parameter) {
            Map<String, Object> map = new HashMap<>();
            List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos = getOutRelCol();
            conditionLinks.forEach((conditionLink, relColumnInfo1s) -> {
                if (!parameter.loaded[conditionLink.index]) {
                    conditionLink.load(parameter);
                }
                List<SqlMapper.BaseColumn> baseColumns = new LinkedList<>();
                for (SqlMapper.BaseTable table : conditionLink.tables) {
                    for (SqlMapper.BaseColumn.RelColumnInfo relColumnInfo : relColumnInfos) {
                        if (!relColumnInfo.tableAlias.equals(table.tableAlias)) continue;
                        for (SqlMapper.BaseColumn column : table.columns) {
                            if (!relColumnInfo.columnName.equals(column.columnName)) continue;
                            baseColumns.add(column);
                        }
                    }
                }

                if (baseColumns.isEmpty()) throw new IllegalStateException("没有找到关联");
                for (SqlMapper.BaseColumn baseColumn : baseColumns) {
                    map.put(baseColumn.objectAlias, parameter.values.get(baseColumn.objectAlias).val);
                }
            });
            parameter.loaded[index] = true;
        }
    }

    public static class Parameter {
        boolean[] loaded;
        Map<String, Value> values = new HashMap<>();

    }

    public static abstract class Value {
        final Object val;
        final Class<?> type;

        public Value(Object val, Class<?> type) {
            this.val = val;
            this.type = type;
        }
    }

    public static class SingleValue extends Value {
        String show;

        public SingleValue(Object val, Class<?> type) {
            super(val, type);
        }
    }

    public static class ForeignValue extends Value {
        Map<String, Object> show;

        public ForeignValue(Object val, Class<?> type) {
            super(val, type);
        }
    }

    public static class MultiValue extends Value {

        public MultiValue(Object val, Class<?> type) {
            super(val, type);
        }
    }
}



