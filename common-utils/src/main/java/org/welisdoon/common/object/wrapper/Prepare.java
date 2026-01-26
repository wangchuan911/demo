package org.welisdoon.common.object.wrapper;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.MyBatisUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static abstract class AbstractPart {
        protected final IDataAccessObject.TableRel rel;
        protected AbstractPart parent;
        protected List<SqlMapper.BaseTable> tables = new LinkedList<>();
        protected List<AbstractPart> parts = new LinkedList<>();
        protected List<AbstractPart> conditionLinks = new LinkedList<>();
        final int index;

        public AbstractPart(int index, IDataAccessObject.TableRel rel) {
            this.index = index;
            this.rel = rel;
        }


        public AbstractPart add(AbstractPart part) {
            parts.add(part);
            part.parent = this;
            return this;
        }

        public AbstractPart add(SqlMapper.BaseTable table) {
            tables.add(table);
            return this;
        }

        /*public <T extends Part> List<T> getPars(Predicate<Part> predicate) {
            return (List) this.parts.stream().filter(part -> predicate.test(part)).collect(Collectors.toList());
        }


        public List<SqlMapper.BaseColumn> findRelColumns(SqlMapper.BaseTable table) {
            return Arrays.stream(table.columns).filter(column -> column.relColumn != null).collect(Collectors.toList());
        }


        public Part findCurrent(SqlMapper.BaseColumn column, boolean findParent) {
            for (SqlMapper.BaseTable table : tables) {
                if (table.tableAlias.equals(column.relColumn.tableAlias)) {
                    return this;
                }
            }
            return findParent && this.parent != null ? this.parent.findCurrent(column, true) : null;
        }

        public Part findParts(SqlMapper.BaseColumn column, boolean findParent, Part exclude) {
            for (Part part : parts) {
                if (exclude == part) break;
                if (part.findCurrent(column, false) != null) {
                    return part;
                }
                for (Part part1 : ((List<Part>) part.parts)) {
                    Part part2 = part1.findCurrent(column, false) != null ? part1 : part1.findParts(column, false, null);
                    if (part2 != null) return part2;
                }
            }
            if (findParent) {
                return parent.findParts(column, true, this);
            }
            return null;
        }*/


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

        protected boolean filterRelColumn(AbstractPart part, List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, final AbstractPart origin) {
            int i = relColumnInfos.size();
            for (SqlMapper.BaseTable table : part.tables) {
                relColumnInfos.removeIf(relColumn -> {
                    if (table.tableAlias.equals(relColumn.tableAlias)) {
                        if (origin != null) {
                            origin.conditionLinks.add(this);
                        }
                        return true;
                    }
                    return false;
                });
            }
            return i > relColumnInfos.size();
        }

        protected void matchedPart(AbstractPart part1, List<AbstractPart> parts1, List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, final AbstractPart origin) {
            for (AbstractPart part : parts) {
                filterRelColumn(part, relColumnInfos, origin);
                if (part == part1) break;
                parts1.add(part);
            }
            if (parent != null)
                parent.matchedPart(part1.parent, parts1, relColumnInfos, origin);
            else
                filterRelColumn(this, relColumnInfos, origin);
        }

        protected void getInnerParts(List<AbstractPart> parts) {
            for (AbstractPart part : this.parts) {
                part.getInnerParts(parts);
                parts.add(part);
            }
        }

        public boolean matchedInPrev(List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, List<AbstractPart> parentParts) {
            relColumnInfos.clear();
            relColumnInfos.addAll(this.getOutRelCol());

            parentParts.clear();
            if (relColumnInfos.isEmpty()) return true;
            parent.matchedPart(this, parentParts, relColumnInfos, this);
            return relColumnInfos.isEmpty();
        }

        public List<AbstractPart> findInnerParts(List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos, List<AbstractPart> parentParts) {
            List<AbstractPart> parentPartsInner = new LinkedList<>();
            List<AbstractPart> matched = new LinkedList<>();
            for (AbstractPart parentPart : parentParts) {
                parentPart.getInnerParts(parentPartsInner);
                for (AbstractPart part : parentPartsInner) {
                    if (filterRelColumn(part, relColumnInfos, this)) {
                        matched.add(part);
                    }
                }
                parentPartsInner.clear();
            }
            return matched;
        }


        protected AbstractPart getRootPart() {
            return parent != null ? parent.getRootPart() : this;
        }

        public void merge() {
            if (parent != null) {
                List<SqlMapper.BaseColumn.RelColumnInfo> relColumnInfos = new LinkedList<>();
                List<AbstractPart> parentParts = new LinkedList<>();
                if (!matchedInPrev(relColumnInfos, parentParts)) {
                    List<AbstractPart> inner = findInnerParts(relColumnInfos, parentParts);
                    if (!relColumnInfos.isEmpty())
                        throw new IllegalStateException(relColumnInfos.stream().map(relColumnInfo -> relColumnInfo.toSql()).collect(Collectors.joining(",")) + "没有匹配到对端");
                }
            }
            for (AbstractPart part : List.copyOf(parts)) {
                part.merge();
            }
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

    public static class MainPart extends AbstractPart {
        boolean[] loaded;

        public MainPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }

        public void init(int partCount) {
            loaded = new boolean[partCount];
        }
    }
    public static class OtherPart extends AbstractPart {

        public OtherPart(int index, IDataAccessObject.TableRel rel) {
            super(index, rel);
        }
    }

}



