package org.welisdoon.common.object.wrapper.database;

import org.welisdoon.common.data.BaseCondition;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname SqlBuilder
 * @Description TODO
 * @Author Septem
 * @Date 20:00
 */
public class SqlBuilder {
    public WithAs withAs = new WithAs();
    public List<Select> select;
    public boolean distinct = false;

    @Override
    public String toString() {
        if (select.size() == 1) {
            select.get(0).distinct = this.distinct;
        } else {
            select.get(0).distinct = false;
        }
        return MessageFormat.format("{0}{2}{1}{3}",
                withAs.toString(),
                select.size() > 1 ? "select * from (" : "",
                select.stream().map(Select::toString).collect(Collectors.joining(" UNION " + (distinct ? "" : " ALL "))),
                select.size() > 1 ? ")" : ""
        );
    }

    public static class WithAs extends LinkedList<WithAs.As> {

        @Override
        public String toString() {
            if (isEmpty()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.size(); i++) {
                if (i != 0) {
                    builder.append(",\n       ");
                } else {
                    builder.append("  WITH ");
                }
                builder.append(this.get(i));
            }
            builder.append("\n");
            return builder.toString();
        }

        public SelectAs add(String name, Select select) {
            SelectAs as = new SelectAs(name, select);
            if (super.add(as)) {
                return as;
            }
            throw new IllegalStateException("写入with as 失败");
        }

        public interface As {
        }

        public static class SelectAs implements As {
            public Select select;
            public String name;

            public SelectAs(String name, Select select1) {
                this.name = name.replaceAll("\\.", "_");
                this.select = select1;

            }

            @Override
            public String toString() {
                Predicate<Property> old = select.propertyFilter;
                select.propertyFilter = property -> true;
                String text = MessageFormat.format("{0} AS (\n{1}\n)", name, select.toString());
                select.propertyFilter = old;
                return text;
            }

            public Join toJoin(String alias, Condition... conditions) {
                Join join = new Join(name, alias, conditions);
                initColumn(join);
                return join;
            }

            public Exists toExists(String alias, boolean isNot, Condition... conditions) {
                Exists join = new Exists(name, alias, isNot, conditions);
                initColumn(join);
                return join;
            }

            public Join toSelect(String alias, Condition... conditions) {
                Select join = new Select(name, alias);
                for (Condition condition : conditions) {
                    join.conditions.add(condition);
                }
                initColumn(join);
                return join;
            }

            protected void initColumn(Join join) {
                this.select.columns.stream().map(property -> {
                    return new Property(new BaseColumn(join, "\"" + property.name + "\""), property.name, property.type);
                }).forEach(join.columns::add);
            }
        }
    }

    public static class Select extends Join {
        BaseCondition.Page page;
        public WithAs withAs = new WithAs();
        public List<Join> joins = new LinkedList<>();
        public boolean distinct = false;
        public boolean count = false;
        public Predicate<Property> propertyFilter = property -> true;

        public Select() {
            this(null, null);
        }

        public Select(String table, String alias) {
            super(table, alias);
        }

        public Select set(String table, String alias, EqualCondition condition, Condition... conditions) {
            if (this.table != null) {
                Join join = new Join(this.table, this.alias);
                join.conditions.add(condition);
                for (Condition condition1 : conditions) {
                    join.conditions.add(condition1);
                }
            }
            this.table = table;
            this.alias = alias;
            return this;
        }

        public WithAs.SelectAs addWithAs(String prefix, Select select1) {
            return withAs.add(prefix + select1.alias + "_" + this.withAs.size(), select1);
        }

        protected Iterator<Join> getAllJoins() {
            List<Join> joins = new LinkedList<>();
            joins.add(this);
            joins.addAll(this.joins);
            for (Condition condition : this.conditions) {
                if (!(condition instanceof Exists)) continue;
                joins.add((Join) condition);
                joins.addAll(((Exists) condition).joins);
            }
            return joins.iterator();
        }

        public Select addCondition(String name, String name2) {
            Property p1 = null, p2 = null, last = null, column;
            Iterator<? extends Join> iterator = getAllJoins();
            while (iterator.hasNext()) {
                Join join = iterator.next();
                if (p1 == null && (column = join.findProperty(name)) != null) {
                    p1 = last = column;
                }
                if (p2 == null && (column = join.findProperty(name2)) != null) {
                    p2 = last = column;
                }
                if (p1 != null && p2 != null) break;
            }
            if (p1 == null) throw new IllegalStateException("没有找到字段:" + name);
            if (p2 == null) throw new IllegalStateException("没有找到字段:" + name2);
            last.column.join.conditions.add(new EqualCondition(p1.column, p2.column));
            return this;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{6}SELECT{5}{4} \n  FROM {0} AS {1} {2} \n WHERE {3}{7}",
                    table,
                    alias,
                    joins.stream().map(Join::toString).collect(Collectors.joining(" ")),
                    conditions.stream().map(Object::toString).collect(Collectors.joining("\n   AND ")),
                    count ? "count(1) AS \"count\"" : Stream.of(columns.stream().filter(propertyFilter), joins.stream().flatMap(join -> join.columns.stream().filter(propertyFilter))).flatMap(propertyStream -> propertyStream.map(Property::toString)).collect(Collectors.joining(",")),
                    !count && distinct ? " distinct " : " ",
                    withAs,
                    page == null ? "" : MessageFormat.format(" limit {0} offset {1}", new Input("_page.pageSize", Number.class), new Input("_page.start", Number.class))
            );
        }
    }

    public static class Property {
        public BaseColumn column;
        public String name;
        public Class<?> type;

        public Property(BaseColumn column, String name, Class<?> type) {
            this.column = column;
            this.name = name;
            this.type = type;
        }

        protected String toColumn() {
            return column.toString();
        }

        protected String toName() {
            return name == null || name.isEmpty() ? column.name : name;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0} AS \"{1}\"",
                    toColumn(), toName());
        }
    }

    public static class Join {
        public String table;
        public String alias;
        public List<Property> columns = new LinkedList<>();
        public List<Condition> conditions = new LinkedList<>();
        public boolean left = false;

        public Join(String table, String alias, Condition... conditions) {
            this.table = table;
            this.alias = alias;
            for (Condition condition : conditions) {
                this.conditions.add(condition);
            }
        }

        public Join addInputCondition(Map<String, Object> params, String prefix) {
            for (SqlBuilder.Property column : this.columns) {
                if (params.containsKey(column.name)) {
                    this.conditions.add(new SqlBuilder.InputCondition(column.column, prefix + column.name, column.type));
                }
            }
            return this;
        }

        public Property findProperty(String property) {
            for (Property column : columns) {
                if (column.name.equals(property)) {
                    return column;
                }
            }
            return null;
        }


        @Override
        public String toString() {
            return MessageFormat.format("\n  {3}JOIN {0} AS {1} \n    ON {2}",
                    table,
                    alias,
                    conditions.stream().map(Object::toString).collect(Collectors.joining("\n   AND ")),
                    left ? "LEFT " : "");
        }
    }

    public interface Condition {
    }

    public static class RangeCondition implements Condition {
        public Column column;
        public List<Column> columns = new LinkedList<>();
        public boolean isNot;

        @Override
        public String toString() {
            return MessageFormat.format("{0} {1} ({2})", column, isNot ? "NOT IN" : "IN", columns.stream().map(Column::toString).collect(Collectors.joining(",")));
        }
    }

    public interface Column {

    }

    public static class BaseColumn implements Column {
        public Join join;
        public String name;

        public BaseColumn(Join join, String name) {
            this.join = join;
            this.name = name;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0}{1}{2}", join.alias == null ? "" : join.alias,
                    join.alias == null || join.alias.isEmpty() ? "" : ".",
                    name);
        }
    }


    public static class ColumnWithDefault extends Property {
        public Object defaultVal;
        public Class<?> type;

        public ColumnWithDefault(BaseColumn column, String name, Object defaultVal, Class<?> type) {
            super(column, name, type);
            this.defaultVal = defaultVal;
        }

        protected String toDefaultVal() {
            if (defaultVal == null) {
                return "null";
            }
            if (defaultVal instanceof Number) {
                return defaultVal.toString();
            }
            return "'" + defaultVal + "'";
        }

        @Override
        public String toColumn() {
            return MessageFormat.format("COALESCE({0},{1})", super.toColumn(), toDefaultVal());
        }
    }

    public static class Input extends BaseColumn {
        Class inputType;

        public Input(String name, Class inputType) {
            super(null, name);
            this.inputType = inputType;
        }

        String toInputType() {
            if (Number.class.isAssignableFrom(inputType)) {
                return "NUMERIC";
            }
            return "VARCHAR";
        }

        @Override
        public String toString() {
            return MessageFormat.format("#'{'{0},jdbcType={1}'}'", name, toInputType());
        }
    }

    public static class SimpleCondition implements Condition {
        public Column aColumn;
        public Column zColumn;
        public String express;

        public SimpleCondition(Column aColumn, Column zColumn, String express) {
            this.aColumn = aColumn;
            this.zColumn = zColumn;
            this.express = express;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0} {1} {2}", aColumn, express, zColumn);
        }
    }

    public static class Exists extends Select implements Condition {
        boolean isNot;

        public Exists(String table, String alias, boolean isNot, Condition... conditions) {
            super(table, alias);
            this.isNot = isNot;
            for (Condition condition : conditions) {
                this.conditions.add(condition);
            }
        }

        @Override
        public String toString() {
            return MessageFormat.format("{5} (\n{4}SELECT 1 \n  FROM {0} AS {1} {2} \n WHERE {3}\n)",
                    table,
                    alias,
                    joins.stream().map(Join::toString).collect(Collectors.joining(" ")),
                    conditions.stream().map(Object::toString).collect(Collectors.joining("\n   AND ")),
                    withAs,
                    isNot ? "NOT EXISTS" : "EXISTS"
            );
        }
    }

    public static class UnionSelect extends LinkedList<Select> {
        boolean distinct;

        public UnionSelect add(String table, String alias, Condition... conditions) {
            Select select = new Select(table, alias);
            for (Condition condition : conditions) {
                select.conditions.add(condition);
            }
            add(select);
            return this;
        }

        @Override
        public String toString() {
            return this.stream().map(select1 -> {
                return MessageFormat.format(
                        "{4}SELECT 1 \n  FROM {0} AS {1} {2} \n WHERE {3}",
                        select1.table,
                        select1.alias,
                        select1.joins.stream().map(Join::toString).collect(Collectors.joining(" ")),
                        select1.conditions.stream().map(Object::toString).collect(Collectors.joining("\n   AND ")),
                        select1.withAs
                );
            }).collect(Collectors.joining("\n UNION" + (distinct ? "\n" : " ALL\n")));
        }
    }

    public static class UnionExists extends UnionSelect implements Condition {
        boolean isNot;

        public UnionExists(boolean isNot) {
            this.isNot = isNot;
        }

        public UnionExists add(String table, String alias, Condition... conditions) {
            super.add(table, alias, conditions);
            return this;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0} (\n{1}\n)",
                    isNot ? "NOT EXISTS" : "EXISTS",
                    super.toString());
        }
    }

    public static class EqualCondition extends SimpleCondition {

        public EqualCondition(Column aColumn, Column zColumn) {
            super(aColumn, zColumn, "=");
        }
    }

    public static class InputCondition implements Condition {
        Column aColumn;
        String name;
        Input input;

        public InputCondition(Column aColumn, String name, Class<?> type) {
            this.aColumn = aColumn;
            this.name = name;
            this.input = new Input(name, type);
        }

        @Override
        public String toString() {
            return aColumn + "=" + input;
        }
    }

    public static class SelectGroup extends LinkedList<SelectGroup.Member> {

        public static class Member {
            public Query query;
            public Level level;

            @Override
            public String toString() {
                return Objects.toString(query);
            }
        }

        public interface Query {
//            boolean containProperty(String s);

            void load(String s, Map<String, Object> map);
        }


        public enum Level {
            Strong, Weak, Multi;
        }
    }

    public static class ConstCondition implements Condition {
        Column aColumn;
        String[] value;
        String operation;

        public ConstCondition(Column aColumn, String operation, String... value) {
            this.aColumn = aColumn;
            this.value = value;
            this.operation = operation;
        }

        @Override
        public String toString() {
            if (value.length == 0) {
                return aColumn + " = null";
            }
            if (value.length > 1) {
                if (operation.equals("=")) {
                    return aColumn + "in ( " + Arrays.stream(value).map(s -> "'" + s + "'").collect(Collectors.joining(" , ")) + " )";
                } else if (operation.equals("!=")) {
                    return aColumn + "NOT IN ( " + Arrays.stream(value).map(s -> "'" + s + "'").collect(Collectors.joining(" , ")) + " )";
                }
            }
            return aColumn + operation + "'" + value[0] + "'";
        }
    }
}
