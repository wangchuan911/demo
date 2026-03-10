package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.common.object.wrapper.IDataAccessObject;
import org.welisdoon.common.object.wrapper.Prepare;
import org.welisdoon.common.object.wrapper.SqlMapper;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname FormatContent
 * @Description TODO
 * @Author Septem
 * @Date 15:15
 */
public class FormatContent implements IDataAccessObject.ObjectScanner {
    public static class FormatContentObject {
        final protected Long target;
        final protected FormatContentObject parent;
        final SqlMapper mapper;
        final static Map<Long, FormatContentObject> CONTENT_OBJECT_MAP = new HashMap<>();
        public final String show;

        FormatContentObject(FormatContentObject parent, SqlContent sqlContent) {
            this.target = sqlContent.metaObject.getId();
            this.parent = parent;
            FormatContent formatContent = new FormatContent();
            this.show = sqlContent.format(formatContent);
            mapper = new SqlMapper(formatContent);
        }

        public static FormatContentObject getObject(MetaObject metaObject) {
            if (metaObject == null) return null;
            if (CONTENT_OBJECT_MAP.containsKey(metaObject.getId())) {
                return CONTENT_OBJECT_MAP.get(metaObject.getId());
            }
            FormatContent.initialization(metaObject.getParent().getType() == ObjectMetaType.Object ? getObject(metaObject.getParent()) : null, metaObject);
            return CONTENT_OBJECT_MAP.get(metaObject.getId());
        }

        public static FormatContentObject getObject(MetaObject metaObject, boolean reload) {
            if (reload && metaObject != null) {
                FormatContent.initialization(metaObject.getParent().getType() == ObjectMetaType.Object ? getObject(metaObject.getParent()) : null, metaObject, true);
                return CONTENT_OBJECT_MAP.get(metaObject.getId());
            }
            return getObject(metaObject);
        }


        public List<Result> query(Map<String, Object> params) {
            return mapper.getBeauty().query(params).stream().map(this::query).collect(Collectors.toList());
        }

        public List<Result> page(Map<String, Object> params, BaseCondition.Page page) {
            return mapper.getBeauty().page(params, page).stream().map(this::query).collect(Collectors.toList());
        }

        public Result query(Object id) {
            return new Result(mapper.getBeauty().query(id));
        }

        public static class Result {
            Map<String, Prepare.Result> map;

            public Result(Map<String, Prepare.Result> map) {
                this.map = map;
            }

            public Map<String, Object> getVal() {
                return getVal(map, "");
            }

            protected Map<String, Object> getVal(Map<String, Prepare.Result> map, String prefix) {
                Map<String, Object> map1 = new HashMap<>();
                map.forEach((s, result) -> {
                    if (result instanceof Prepare.Value) {
                        map1.put(s, result.getVal());
                    } else if (result instanceof Prepare.Values) {
                        map1.put(s, getVal((Map<String, Prepare.Result>) result, append(prefix, s)));
                    } else if (result instanceof Prepare.MultiValues) {
                        List<Object> list = new LinkedList<>();
                        ListIterator<Prepare.Values> iterator = ((List<Prepare.Values>) result.getVal()).listIterator();
                        Prepare.Values values;
                        int idx;
                        while (iterator.hasNext()) {
                            idx = iterator.nextIndex();
                            values = iterator.next();
                            list.add(this.getVal(values, append(prefix, s, "[" + idx + "]")));
                        }
                        map1.put(s, list);
                    }
                });
                return map1;
            }

            protected String append(String... keys) {
                return Arrays.stream(keys).filter(StringUtils::isNotEmpty).collect(Collectors.joining("."));
            }
        }
    }

    /*final protected VirtualPart tablePart;
    private LinkedList<Part> current = new LinkedList<>();
    protected int count = 0;

    public FormatContent() {
        tablePart = new VirtualPart(this, LinkMetaType.ObjConstructor);
        current.addLast(tablePart);
    }

    public int getTableCount() {
        return count;
    }

    public void addTablePart(Part part) {
        Assert.isTrue(current.getLast() instanceof VirtualPart, "上级必须为虚拟");
        if (part instanceof VirtualPart) {
            if (current.size() > 0) {
                ((VirtualPart) current.getLast()).addChildren(part);
            }
            current.addLast(part);
            return;
        }
        if (part instanceof LeafPart) {
            ((LeafPart) part).index = count++;
        }
        ((VirtualPart) current.getLast()).addChildren(part);
    }

    public void pullTablePart() {
        current.removeLast();
    }

    public void addColumnPart(Part.FormatColumn sqlAlias) {
        ((LeafPart) current.getLast()).columns.add(sqlAlias);
    }


//        protected List<Part.FormatColumn> getColumnParts() {
//            return columnParts;
//        }

    protected List<Part> getTablePart() {
        return tablePart.getChildren();
    }

    public static class VirtualPart extends Part implements MetaPrototype.Parent<Part> {

        final List<Part> children = new LinkedList<>();

        public VirtualPart(FormatContent content, LinkMetaType type) {
            super(content, new SqlAlias(String.valueOf(System.currentTimeMillis()), ""), type);
        }

        @Override
        public List<Part> getChildren() {
            return children;
        }
    }

    public static class LeafPart extends Part {
        int index;
        final List<String> condition;
        final List<FormatColumn> columns;

        public LeafPart(FormatContent content, SqlAlias sqlAlias, List<String> condition, List<FormatColumn> column, LinkMetaType type) {
            super(content, sqlAlias, type);
            this.condition = condition == null ? List.of() : condition;
            this.columns = column;
        }

        public int getIndex() {
            return index;
        }

        public static LeafPart find(List<Part> part, int index) {
            LeafPart part2;
            for (Part part1 : part) {
                part2 = find(part1, index);
                if (part2 != null) {
                    return part2;
                }
            }
            return null;
        }

        public static LeafPart find(Part part, int index) {
            if (part instanceof VirtualPart) {
                return find(((VirtualPart) part).getChildren(), index);
            } else if (part instanceof LeafPart && ((LeafPart) part).getIndex() == index) {
                return (LeafPart) part;
            }
            return null;
        }

        public static LeafPart findFirst(Part part) {
            if (part instanceof VirtualPart) {
                return findFirst(((VirtualPart) part).getChildren().get(0));
            } else if (part instanceof LeafPart) {
                return (LeafPart) part;
            }
            return null;
        }

        public List<String> getCondition() {
            return condition;
        }

        public List<FormatColumn> getColumns() {
            return columns;
        }

    }

    public static class Part extends SqlAlias implements MetaPrototype.Child<Part> {
        final LinkMetaType type;
        Part parent;

        protected Part(FormatContent content, SqlAlias sqlAlias, LinkMetaType type) {
            super(sqlAlias.getAlias(), sqlAlias.getTarget());
            this.type = type;
        }


        public LinkMetaType getType() {
            return type;
        }


        @Override
        public Part getParent() {
            return parent;
        }

        @Override
        public MetaPrototype.Child setParent(Part parent) {
            return this;
        }

        public static class FormatColumn extends SqlAlias {
            DataObject.Field field;

            public FormatColumn(String alias, String target, DataObject.Field field) {
                super(alias, target);
                this.field = field;
            }

            public DataObject.Field getField() {
                return field;
            }
        }


    }*/
    SqlMapper sqlMapper;
    List<Map.Entry<SqlJoiner, List<Object>>> cache = new LinkedList<>();

    List<Object> get(SqlJoiner sqlJoiner) {
        for (Map.Entry<SqlJoiner, List<Object>> sqlJoinerListEntry : cache) {
            if (sqlJoiner == sqlJoinerListEntry.getKey()) {
                return sqlJoinerListEntry.getValue();
            }
        }
        return List.of();
    }


    public List<Object> init(SqlJoiner sqlJoiner) {
        List<Object> list = new LinkedList<>();
        cache.add(Map.entry(sqlJoiner, list));
        return list;
    }

    protected static boolean initialization(FormatContentObject parent, MetaObject metaObject) {
        return initialization(parent, metaObject, false);
    }

    protected synchronized static boolean initialization(FormatContentObject parent, MetaObject metaObject, boolean reload) {
        FormatContentObject content = FormatContentObject.CONTENT_OBJECT_MAP.get(metaObject.getId());
        if (content != null && !reload) {
            return true;
        }
        FormatContentObject formatContentObject = new FormatContentObject(parent, new SqlContent(metaObject));
        if (content != null && reload && Objects.equals(formatContentObject.show, content.show)) {
            return true;
        }
        FormatContentObject.CONTENT_OBJECT_MAP.put(metaObject.getId(), formatContentObject);
        MetaUtils.getInstance().getMetaObjectDao().list(new MetaObjectCondition()
                .<MetaObjectCondition>setQuery("FIND_EXTEND_OBJECT")
                .setData(new MetaObject()
                        .<MetaObject>setTypeId(ObjectMetaType.Object.getId())
                        .setParentId(metaObject.getId())))
                .forEach(metaObject1 -> {
                    FormatContent formatContent = new FormatContent();
                    formatContent.initialization(formatContentObject, metaObject1, reload);
                });
        return true;
    }

    public static void initialization() {
        MetaUtils.getInstance().getMetaObjectDao().list(new MetaObjectCondition()
                .<MetaObjectCondition>setData(new MetaObject()
                        .setTypeId(ObjectMetaType.Object.getId()))
                .setQuery("FIND_BASE_OBJECT")).forEach(metaObject -> {
            FormatContent formatContent = new FormatContent();
            formatContent.initialization(null, metaObject);
        });
    }

    @Override
    public void scanTableAnnotation(List<IDataAccessObject.Model.TableVO> tableList) {
        cache.stream().map(entry -> {
            SqlJoiner sqlJoiner = entry.getKey();
            IDataAccessObject.TableRel rel;
            switch (sqlJoiner.getType()) {
                case SqlToJoinOfWeakRel:
                    rel = IDataAccessObject.TableRel.Weak;
                    break;
                case SqlToJoinOfMultiDataRel:
                    rel = IDataAccessObject.TableRel.Multi;
                    break;
                default:
                    rel = IDataAccessObject.TableRel.Strong;
                    break;
            }
            if (sqlJoiner.table.getAlias().matches("T\\d+(\\_1)*") && sqlJoiner.getParent() instanceof SqlJoiner) {
                rel = sqlJoiner.getParent().getType() == LinkMetaType.SqlToJoinOfStrongRel || sqlJoiner.getParent().getType() == LinkMetaType.ObjConstructor ? IDataAccessObject.TableRel.Strong : rel;
            }
            return new IDataAccessObject.Model.TableVO(
                    entry.getValue().stream().filter(o -> o instanceof IDataAccessObject.Model.TableVO.ColumnVO).map(o -> (IDataAccessObject.Model.TableVO.ColumnVO) o).toArray(IDataAccessObject.Model.TableVO.ColumnVO[]::new),
                    String.format("%s.%s %s", "this", sqlJoiner.table.getTarget(), sqlJoiner.table.getAlias()),
                    "datasource",
                    entry.getValue().stream().filter(o -> o instanceof String).map(o -> (String) o).toArray(String[]::new),
                    sqlJoiner.table.getAlias().matches("T\\d+") ? null : sqlJoiner.table.getAlias().substring(0, sqlJoiner.table.getAlias().lastIndexOf("_")),
                    rel);
        }).forEach(tableList::add);
    }
}
