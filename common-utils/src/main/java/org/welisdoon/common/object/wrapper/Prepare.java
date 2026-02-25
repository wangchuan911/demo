package org.welisdoon.common.object.wrapper;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.MyBatisUtils;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname Prepare
 * @Description TODO
 * @Author Septem
 * @Date 8:48
 */
public class Prepare {
    public final static String SPLITTER = ".";
    static final String logFormat = "%s==> %-6s ==>: %-6s [ %s ] %n";
    final public String sql;
    final public List<Object> params;

    Prepare(String sql, List<Object> params) {
        this.sql = sql.replace(Beauty.MARK_1, "").replace(Beauty.MARK_2, "");
        this.params = params;
    }

    Prepare(String prepareSql, Map<String, Object> params) {
        this(prepareSql.replaceAll(MyBatisUtils.PATTERN_STRING, "?"), new LinkedList<>());
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
    }

    static String valToSql(Object o) {
        if (o instanceof Number) {
            return o.toString();
        } else if (o instanceof Date || o instanceof Calendar) {
            String format = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String formattedDate = sdf.format(o instanceof Date ? ((Date) o).getTime() : ((Calendar) o).getTime());
            return MessageFormat.format("to_date(''{0}'',''{1}'')", formattedDate, format);
        } else if (o instanceof ChronoLocalDate) {
            return MessageFormat.format("to_date(''{0}'',''{1}'')", ((ChronoLocalDate) o).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), DateTimeFormatter.ISO_LOCAL_DATE_TIME.toString());
        } else {
            return MessageFormat.format("''{0}''", o.toString());
        }
    }

    void log(Object prefix) {
        System.out.printf(logFormat, prefix, "TO DB", "SQL", sql);
        System.out.printf(logFormat, prefix, "TO DB", "PARAMS", params.stream().map(o -> MessageFormat.format("{0}({1})", Objects.toString(o), o.getClass().getSimpleName())).collect(Collectors.joining(" , ")));
        String sql2 = sql;
        for (int i = 0; sql2.contains("?"); i++) {
            sql2 = sql2.replaceFirst("\\?", valToSql(params.get(i)));
        }
        System.out.printf(logFormat, prefix, "TO LOG", "SQL", sql2);
    }

    public static abstract class Part {
        protected final IDataAccessObject.TableRel rel;
        protected Part parent;
        protected List<SqlMapper.BaseTable> tables = new LinkedList<>();
        protected List<Part> parts = new LinkedList<>();
        protected Map<Part, List<SqlMapper.BaseColumn.RelColumnInfo>> conditionLinks = new HashMap<>();
        final int index;
        final Map<Parameter, Map<String, Object>> paramLocal = new HashMap<>();

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

        public void query(Parameter parameter) {
            if (!parameter.loaded.contains(this)) {
                parameter.loaded.add(this);
                if (!paramLocal.containsKey(parameter))
                    paramLocal.put(parameter, new HashMap<>());
                load(parameter);
                paramLocal.remove(parameter);
            }

            for (Part part : parts) {
                part.query(parameter);
            }
        }

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
            return String.format("%-15s[%2d]", getClass().getSimpleName(), index);
        }

        public void load(Runnable runnable) {
            runnable.run();
        }

        void query(String sql, Map<String, Object> map) {
            List<Object> params = new LinkedList<>();
            Map<String, String> format = new HashMap<>();
//            Map<String, String> format2 = new HashMap<>();
            MyBatisUtils.readSqlTemplate(sql, (s, s2) -> {
                Object o = map.get(s);
                if (o instanceof List) {
                    params.addAll((Collection<?>) o);
                    format.put(MessageFormat.format("= #'{'{0}'}'", s), MessageFormat.format("in ({0})", ((List<?>) o).stream().map(o1 -> "?").collect(Collectors.joining(","))));
//                    format2.put(MessageFormat.format("= #'{'{0}'}'", s), MessageFormat.format("in ({0})", ((List<?>) o).stream().map(this::valToSql).collect(Collectors.joining(","))));
                } else {
                    params.add(o);
                    format.put(MessageFormat.format("#'{'{0}'}'", s), "?");
//                    format2.put(MessageFormat.format("#'{'{0}'}'", s), valToSql(o));
                }
            });
            String sql2 = sql;
            for (Map.Entry<String, String> entry : format.entrySet()) {
                sql2 = sql2.replace(entry.getKey(), entry.getValue());
            }
            Prepare prepare = new Prepare(sql2, params);
            prepare.log(this);
            /*sql2 = sql;
            for (Map.Entry<String, String> entry : format2.entrySet()) {
                sql2 = sql2.replace(entry.getKey(), entry.getValue());
            }
            sql2 = sql2.replace(Beauty.MARK_1, "").replace(Beauty.MARK_2, "");
            System.out.printf(logFormat, this, "TO LOG", "SQL", sql2);*/
        }


    }

    public static class MainPart extends Part {

        public MainPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        public void load(Parameter parameter) {
            this.query(toSql().replace(Beauty.WHERE, MessageFormat.format(Beauty.EQUAL_PARAM, tables.get(0).columns[0].toSql(), tables.get(0).columns[0].objectAlias)), parameter);
        }

        void query(String sql, Parameter parameter) {
            query(sql, paramLocal.get(parameter));
            tables.forEach(table -> Arrays.stream(table.columns).forEach(column -> {
                parameter.values.put(column.objectAlias, new Value(column.objectAlias + "_Val", column.dataType));
            }));
        }

    }

    public static abstract class AbstractLeafPart extends Part {

        String getParentNode(String value) {
            return value.substring(0, value.lastIndexOf(SPLITTER));
        }

        String getUpperNode(String value) {
            return value.substring(0, value.lastIndexOf(SPLITTER));
        }

        public AbstractLeafPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        @Override
        public void load(Parameter parameter) {
            List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos = getOutRelCol();

            for (Map.Entry<Part, List<SqlMapper.BaseColumn.RelColumnInfo>> partListEntry : conditionLinks.entrySet()) {
                Part conditionLink = partListEntry.getKey();
                List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfo1s = partListEntry.getValue();
                if (!parameter.loaded.contains(conditionLink)) {
                    System.out.println("加载未加载sql:" + conditionLink.index);
                    conditionLink.query(parameter);
                }

                Collection<SqlMapper.BaseColumn> baseColumns = new HashSet<>();
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
                    System.out.println(this + "-->" + baseColumn.objectAlias);
                    setSqlParam(baseColumn, parameter);
                }
            }
            this.query(toSql().replace(Beauty.AND + Beauty.WHERE, ""), parameter);
        }

        void query(String sql, Parameter parameter) {
            Map<String, Object> map2 = new HashMap<>();
            Map<String, Object> params = paramLocal.get(parameter);
            params.remove("@@");
            Map.Entry<String, Map<Result, Object>> valueEntry = null;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    if (valueEntry != null) throw new IllegalStateException("存在多个");
                    valueEntry = (Map.Entry) entry;
                } else {
                    map2.put(entry.getKey(), entry.getValue());
                }
            }
            query(sql, parameter, map2, valueEntry);
        }

        abstract void query(String sql, Parameter parameter, Map<String, Object> singleParams, Map.Entry<String, Map<Result, Object>> multiParams);


        void setSqlParam(SqlMapper.BaseColumn baseColumn, Parameter parameter) {

            Map<String, Object> map = paramLocal.get(parameter);
            if (!baseColumn.objectAlias.contains(SPLITTER)) {
                map.put(baseColumn.objectAlias, parameter.values.get(baseColumn.objectAlias).getVal());
                return;
            }
            //获取前缀 当前PART 节点 父节点.数组节点.属性==> 父节点
            String parentNode = getParentNode(tables.get(0).columns[0].objectAlias);
            String upperNode = getUpperNode(baseColumn.objectAlias);
            //如果当前取的值在PART节点，将额外记录 value节点，方便将结果赋值
            if (parentNode != null && parentNode.equals(upperNode)) {
                Map.Entry<String, String> prefix2 = (Map.Entry) map.get("@@");
                //只记录一个value,原来的恢复
                if (prefix2 != null) {
                    Map<Result, List<Object>> resultListMap = (Map) map.get(prefix2.getValue());
                    map.put(prefix2.getValue(), resultListMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
                }
                Map<Result, Object> resultListMap = new HashMap<>();
                for (Result result : parameter.getLowerValue(upperNode)) {
                    result.findLowerValue(baseColumn.objectAlias.substring(upperNode.length() + SPLITTER.length()), result1 -> {
                        if (resultListMap.put(result, result1.getVal()) != null)
                            throw new IllegalStateException("存在多个数据");
                    });
                }
                map.put(baseColumn.objectAlias, resultListMap);
                map.put("@@", Map.entry(parentNode, baseColumn.objectAlias));
                return;

            }
            map.put(baseColumn.objectAlias, parameter.getLowerValue(baseColumn.objectAlias).stream().map(Result::getVal).collect(Collectors.toList()));
        }
    }

    public static class LeafSinglePart extends AbstractLeafPart {

        public LeafSinglePart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        void query(String sql, Parameter parameter, Map<String, Object> singleParams, Map.Entry<String, Map<Result, Object>> multiParams) {
            SqlMapper.BaseColumn baseColumn = tables.get(0).columns[0];
            if (baseColumn.objectAlias.contains(SPLITTER)) {
                String parentNode = getParentNode(baseColumn.objectAlias);
                if (multiParams != null)
                    for (Map.Entry<Result, Object> valueObjectEntry : multiParams.getValue().entrySet()) {
                        singleParams.put(multiParams.getKey(), valueObjectEntry.getValue());
                        query(sql, singleParams, (Values) valueObjectEntry.getKey());
                    }
                else {
                    parameter.getLowerValue(parentNode).forEach(result -> {
                        query(sql, singleParams, (Values) result);
                    });
                }
            } else {
                if (multiParams != null) {
                    for (Map.Entry<Result, Object> valueObjectEntry : multiParams.getValue().entrySet()) {
                        singleParams.put(multiParams.getKey(), valueObjectEntry.getValue());
                        query(sql, singleParams, ((Values) valueObjectEntry.getKey()));
                    }
                } else {
                    query(sql, singleParams, parameter.values);
                }
            }
        }

        void query(String sql, Map<String, Object> singleParams, Values values) {
            query(sql, singleParams);
            tables.forEach(table -> Arrays.stream(table.columns).forEach(column -> {
                values.put(column.objectAlias.substring(column.objectAlias.lastIndexOf(SPLITTER) + SPLITTER.length()), new Value(column.objectAlias + "_Val", column.dataType));
            }));
        }
    }

    public static class LeafMultiPart extends AbstractLeafPart {

        public LeafMultiPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        String getMultiNodeName(String value) {
            StringBuilder prefix = new StringBuilder(value).delete(value.lastIndexOf(SPLITTER), value.length());
            int deep = prefix.lastIndexOf(SPLITTER);
            if (deep >= 0) {
                prefix.delete(0, deep + SPLITTER.length());
            }
            return prefix.toString();
        }

        String getParentNode(String value) {
            value = value.substring(0, value.lastIndexOf(SPLITTER));
            int i = value.lastIndexOf(SPLITTER);
            if (i < 0) {
                return null;
            }
            return value.substring(0, i);
        }

        int getPrefixNodeLevel(String value) {
            int offset = 0;
            int index = 0;
            int count = 0;
            while (index >= 0) {
                index = value.indexOf(SPLITTER, offset);
                if (index >= 0) count++;
                else offset = index + SPLITTER.length();
            }
            return count;
        }

        void query(String sql, Parameter parameter, Map<String, Object> singleParams, Map.Entry<String, Map<Result, Object>> multiParams) {
            SqlMapper.BaseColumn baseColumn = tables.get(0).columns[0];
            String parentNode = getParentNode(baseColumn.objectAlias);
            String node = getMultiNodeName(baseColumn.objectAlias);
            if (multiParams != null)
                for (Map.Entry<Result, Object> valueObjectEntry : multiParams.getValue().entrySet()) {
                    singleParams.put(multiParams.getKey(), valueObjectEntry.getValue());
                    MultiValues multiValues = new MultiValues();
                    ((Values) valueObjectEntry.getKey()).put(node, multiValues);
                    query(sql, singleParams, multiValues);
                }
            else {
                parameter.getLowerValue(parentNode).forEach(result -> {
                    MultiValues multiValues = new MultiValues();
                    ((Values) result).put(node, multiValues);
                    query(sql, singleParams, multiValues);
                });
            }
        }

        void query(String sql, Map<String, Object> map, MultiValues values) {
            query(sql, map);
            for (int i = 0; i < 2; i++) {
                Values values1 = new Values();
                int finalI = i;
                tables.forEach(table -> Arrays.stream(table.columns).forEach(column -> {
                    values1.put(column.objectAlias.substring(column.objectAlias.lastIndexOf(SPLITTER) + SPLITTER.length()), new Value(column.objectAlias + "_Val_" + finalI, column.dataType));
                }));
                values.add(values1);
            }
        }

    }

    public static class Parameter {
        Values values = new Values();
        Set<Part> loaded = new HashSet<>();


        public List<Result> getLowerValue(String key) {
            if (StringUtils.isEmpty(key)) return List.of(values);
            List<Result> list = new LinkedList<>();
            values.findLowerValue(key, value -> {
                list.add(value);
            });
            return list;
        }
    }

    interface Result {
        void findLowerValue(String key, Consumer<Result> val);

        default String[] getKeyAndSuffix(String key) {
            if (key.contains(SPLITTER)) {
                return new String[]{key.substring(0, key.indexOf(SPLITTER)), key.substring(key.indexOf(SPLITTER) + SPLITTER.length())};
            }
            return new String[]{key, ""};
        }

        Object getVal();
    }

    public static class Values extends HashMap<String, Result> implements Result {

        @Override
        public void findLowerValue(String key, Consumer<Result> val) {
            if (StringUtils.isEmpty(key)) {
                val.accept(this);
            }
            String[] keys = getKeyAndSuffix(key);
            String[] arrKey = getArrIndex(keys[0]);
            Result result = get(arrKey[0]);
            if (result == null) return;
            result.findLowerValue(arrKey[1] + keys[1], val);
        }

        @Override
        public Object getVal() {
            return this;
        }

        String[] getArrIndex(String key) {
            boolean isArr = key.contains("[") && key.endsWith("]");
            if (isArr) {
                return new String[]{key.substring(0, key.indexOf("[")), key.substring(key.indexOf("["))};
            }
            return new String[]{key, ""};
        }
    }

    public static class MultiValues implements Result {
        List<Values> values = new LinkedList<>();

        @Override
        public void findLowerValue(String key, Consumer<Result> val) {
            int[] i = getArrIndex(key);
            if (i.length == 0)
                values.forEach(values1 -> {
                    values1.findLowerValue(key, val);
                });
            else if (i.length == 1)
                values.get(i[0]).findLowerValue(key.substring(key.indexOf("]") + 1), val);
            else if (i.length == 2) {
                ListIterator<Values> iterator = values.listIterator();
                while (iterator.hasNext()) {
                    if (iterator.nextIndex() >= i[0]) {
                        iterator.next().findLowerValue(key.substring(key.indexOf("]") + 1), val);
                    } else {
                        iterator.next();
                    }
                    if (iterator.nextIndex() > i[1]) break;
                }
            }

        }

        void add(Values values) {
            this.values.add(values);
        }

        int[] getArrIndex(String key) {
            boolean isArr = key.startsWith("[") && key.contains("]");
            if (!isArr) return new int[0];
            String val = key.substring(1, key.indexOf("]"));
            if (val.contains("-")) {
                return Arrays.stream(val.split("-")).mapToInt(Integer::parseInt).toArray();
            }
            return new int[]{Integer.parseInt(val)};
        }

        @Override
        public Object getVal() {
            return values;
        }

        @Override
        public String toString() {
            return " [ " + values.stream().map(AbstractMap::toString).collect(Collectors.joining(" , ")) + " ] ";
        }
    }

    public static class Value implements Result {
        final Object val;
        final Class<?> type;

        public Value(Object val, Class<?> type) {
            this.type = type;
            this.val = val;
        }

        public Object getVal() {
            return TypeUtils.cast(val, (Type) type, ParserConfig.getGlobalInstance());
        }

        public void findLowerValue(String key, Consumer<Result> val) {
            if (StringUtils.isEmpty(key)) val.accept(this);
        }

        @Override
        public String toString() {
            return Objects.toString(val);
        }
    }
}



