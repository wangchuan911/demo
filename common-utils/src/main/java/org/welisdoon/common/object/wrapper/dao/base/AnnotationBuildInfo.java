package org.welisdoon.common.object.wrapper.dao.base;

import org.apache.commons.collections4.CollectionUtils;
import org.welisdoon.common.object.wrapper.dao.demo.IDemoRfsEntity;
import org.welisdoon.common.object.wrapper.database.SqlBuilder;
import org.welisdoon.common.object.wrapper.dao.annotation.Database;
import org.welisdoon.common.object.wrapper.dao.demo.IDemoLinkEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @Classname AnnotationBuildInfo
 * @Description TODO
 * @Author Septem
 * @Date 17:00
 */
class AnnotationBuildInfo implements IEntity.BuildInfo {
    static final Pattern COLUMN = Pattern.compile("(\\w+)\\.(\\w+)");
    static final Pattern TABLE = Pattern.compile("(\\w+\\.\\w+)\\s+(\\w+)");
    static final Map<Class<? extends IEntity>, List<AnnotationBuildInfo>> cache = new HashMap<>();

    Class<? extends IEntity> type;
    String name;
    AnnotationBuildInfo parent;
    List<AnnotationBuildInfo> children = new LinkedList<>();
    Database.Part[] parts;

    protected AnnotationBuildInfo(Class<? extends IEntity> aClass) {
        type = aClass;
        if (!aClass.isInterface()) throw new IllegalStateException("只支持interface");
        for (Class<?> anInterface : aClass.getInterfaces()) {
            if (IEntity.class.isAssignableFrom(anInterface) && IEntity.class != anInterface) {
                parent = AnnotationBuildInfo.get((Class<? extends IEntity>) anInterface);
                parent.children.add(this);
                break;
            }
        }
        cache.compute(getMain(aClass), (aClass1, buildInfos) -> {
            buildInfos = buildInfos != null ? buildInfos : new LinkedList<>();
            buildInfos.add(this);
            return buildInfos;
        });
        List<SqlBuilder.Select> selects = new LinkedList<>();
        if (parent == null) {
            Database.Main main = aClass.getAnnotation(Database.Main.class);
            this.name = main.feature().alias();
            parts = main.parts();
        } else {
            Database.Extra extra = aClass.getAnnotation(Database.Extra.class);
            this.name = extra.feature().alias();
            parts = extra.parts();
        }
    }

    static Matcher readTableInfo(String express) {
        Matcher tableMatcher = TABLE.matcher(express);
        tableMatcher.find();
        return tableMatcher;
    }

    static Matcher readColumnInfo(String express) {
        Matcher tableMatcher = COLUMN.matcher(express);
        tableMatcher.find();
        return tableMatcher;
    }

    public static class QueryParameter {
        public boolean noExists = false;
        SqlBuilder.Select main;
        final String prefix;
        final Map<String, SqlBuilder.Column> colsRelMapper = new HashMap<>();
        SqlBuilder.Select exists;
        final Map<String, Object> condition;

        public QueryParameter(String prefix, Map<String, Object> condition) {
            this.condition = condition;
            this.prefix = prefix == null || prefix.isEmpty() ? "" : prefix.endsWith(".") ? prefix : prefix + '.';
        }

        public QueryParameter(Map<String, Object> condition) {
            this("", condition);
        }

        public QueryParameter subParameter(String prefix) {
            Map<String, Object> map = new HashMap();
            String key = prefix + ".";
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                if (entry.getKey().startsWith(key)) {
                    map.put(entry.getKey().substring(key.length()), entry.getValue());
                }
            }
            QueryParameter queryParameter = new QueryParameter(this.prefix + prefix, map);
            queryParameter.noExists = true;
            return queryParameter;
        }

        void addAttrToMapper(Database.Part part, SqlBuilder.Join join) {
            for (Database.Attr attr1 : part.attrs()) {
                for (SqlBuilder.Property property : join.columns) {
                    if (property.name.equals(attr1.alias())) {
                        colsRelMapper.put(attr1.alias(), property.column);
                    }
                }
            }
        }


    }

    protected List<Database.Attr> getRelAttr(Database.Part part, QueryParameter cache) {
        String alias = null;
        List<Database.Attr> relAttrs = null;
        for (Database.Attr attr : part.attrs()) {
            if (attr.relAttr().isEmpty()) continue;
            if (!cache.colsRelMapper.containsKey(attr.relAttr())) continue;
            alias = alias != null ? alias : readTableInfo(part.name()).group(2);
            relAttrs = relAttrs != null ? relAttrs : new LinkedList<>();
            relAttrs.add(attr);
        }
        if (CollectionUtils.isEmpty(relAttrs)) throw new IllegalStateException("存在未关联的对象" + part.name());
        return relAttrs;
    }

    protected void setExists(SqlBuilder.Select select, QueryParameter cache, Iterator<Database.Part> parts) {
        List<Database.Attr> relAttrs;
        Database.Part part;
        if (cache.exists != null) {
            return;
        }
        if (cache.noExists) {
            cache.exists = cache.main;
            return;
        } else if (parts.hasNext()) {
            part = parts.next();
            SqlBuilder.Exists exists = toExists(part, false);
            cache.exists = exists;
            //添加入参,更新strong
            initParameter(exists, cache, part);
            relAttrs = getRelAttr(part, cache);
            initCondition(exists, relAttrs, cache.colsRelMapper);
            select.conditions.add(exists);
        }
    }

    //
    // 生成 脚本
    //  select t1.col1
    //    from ta t1
    //   where exists (
    //         select 1
    //          from tb t2
    //          join tc t3 on t2.col1=t3.col1
    //          .....
    //         where t1.col1=t2.col1
    //   )
    // 只返回id
    protected void buildQuerySelect(QueryParameter cache) {
        final SqlBuilder.Select select;
        Database.Part part;
        Iterator<Database.Part> parts = Arrays.stream(this.parts).filter(Database.Part::required).iterator();

        initCondition(cache);
        if (parent == null) {
            part = parts.next();
            select = toSelect(part);
            String idKey = type.getAnnotation(Database.Main.class).id();
            select.propertyFilter = property -> idKey.equals(property.name);
            //添加入参,更新strong
            initParameter(select, cache, part);
            cache.main = select;
        } else {
            parent.buildQuerySelect(cache);
            select = cache.main;
        }
        setExists(select, cache, parts);
        for (; parts.hasNext(); ) {
            part = parts.next();
            toJoin(part, cache, cache.exists);
        }
        if (parts.hasNext()) throw new IllegalStateException("存在未关联的对象" + parts.next().name());

        parts = Arrays.stream(this.parts).filter(part1 -> !part1.required()).iterator();
        for (; parts.hasNext(); ) {
            part = parts.next();
            if (!hasParams(part, cache.condition)) continue;
            toJoin(part, cache, cache.exists);
        }

        if (parts.hasNext()) throw new IllegalStateException("存在未关联的对象" + parts.next().name());

        Iterator<Database.Relation> iterator = Arrays.stream(type.getAnnotationsByType(Database.Relation.class)).iterator();
        Database.Relation relation;
        while (iterator.hasNext()) {
            relation = iterator.next();
            if (!hasParams(relation, cache.condition)) continue;
            AnnotationBuildInfo annotationBuildInfo = AnnotationBuildInfo.get(relation.target());
            QueryParameter cache1 = cache.subParameter(relation.alias());
            annotationBuildInfo.buildQuerySelect(cache1);
            SqlBuilder.Select select1 = cache1.main;
            SqlBuilder.WithAs.As as = select.withAs.add(cache.prefix + select1.alias + "_" + select.withAs.size(), select1);
            Map<Database.AttrToAttr, SqlBuilder.Property> attrToAttrPropertyMap = new HashMap<>();
            SqlBuilder.Join join1 = withAsFindJoin(select, relation, attrToAttrPropertyMap);
            if (join1 == null) throw new IllegalStateException(as + "没有跟当前表有关联");
            attrToAttrPropertyMap.forEach((attrToAttr, property) -> {
                initWithAsCondition(attrToAttr, property, as, join1);
            });

        }
        System.out.println(type.getAnnotationsByType(Database.Relations2.class));
        Iterator<Database.Relation2> iterator2 = Arrays.stream(type.getAnnotationsByType(Database.Relation2.class)).iterator();
        while (iterator2.hasNext()) {
            Database.Relation2 relation2 = iterator2.next();
            if (!hasParams(relation2, cache.condition)) continue;
            SqlBuilder.UnionExists exists = new SqlBuilder.UnionExists(false);
            SqlBuilder.Join join1 = Stream.of(Stream.of(cache.main), cache.main.joins.stream()).flatMap(stream -> stream).filter(join -> join.columns.stream().anyMatch(property -> property.name.equals(relation2.selfAttr()))).findFirst().orElseThrow(() -> new IllegalStateException("没有找到对应的属性:" + relation2.selfAttr()));
            for (Database.AttrToAttr2 attrToAttr2 : relation2.rel()) {
                AnnotationBuildInfo annotationBuildInfo = AnnotationBuildInfo.get(attrToAttr2.targetType());
                QueryParameter cache1 = cache.subParameter(relation2.alias());
                annotationBuildInfo.buildQuerySelect(cache1);
                SqlBuilder.Select select1 = cache1.main;
                exists.add(select1);
                select1.conditions.add(new SqlBuilder.EqualCondition(select1.findProperty(attrToAttr2.targetAttr()).column, join1.findProperty(relation2.selfAttr()).column));
            }
            join1.conditions.add(exists);
        }
    }


    void initCondition(QueryParameter queryParameter) {
        Map<String, Object> condition = queryParameter.condition;
        for (Database.Relation relation : type.getAnnotationsByType(Database.Relation.class)) {
            for (Database.AttrToAttr attrToAttr : relation.rel()) {
                String key = relation.alias() + "." + attrToAttr.target();
                if (condition.containsKey(key)) {
                    String key2 = attrToAttr.self();
                    System.out.println(String.format("%s==>%s", key, key2));
                    condition.put(key2, condition.remove(key));
                }
            }
        }
        for (Database.Relation2 relation : type.getAnnotationsByType(Database.Relation2.class)) {
            for (Database.AttrToAttr2 attrToAttr2 : relation.rel()) {
                String key = relation.alias() + "." + attrToAttr2.targetAttr();
                if (condition.containsKey(key)) {
                    String key2 = relation.selfAttr();
                    System.out.println(String.format("%s==>%s", key, key2));
                    condition.put(key2, condition.remove(key));
                }
            }
        }
    }

    protected SqlBuilder.Join withAsFindJoin(SqlBuilder.Select select, Database.Relation relation, Map<Database.AttrToAttr, SqlBuilder.Property> attrToAttrPropertyMap) {
        SqlBuilder.Join join1 = null;
        for (Database.AttrToAttr attrToAttr : relation.rel()) {
            for (SqlBuilder.Condition condition : select.conditions) {
                if (condition instanceof SqlBuilder.Exists) {
                    withAsFindJoin((SqlBuilder.Exists) condition, relation, attrToAttrPropertyMap);
                }
            }
            for (SqlBuilder.Property property : select.columns) {
                if (attrToAttr.self().equals(property.name)) {
                    join1 = select;
                    attrToAttrPropertyMap.put(attrToAttr, property);
                }
            }
            for (SqlBuilder.Join join : select.joins) {
                for (SqlBuilder.Property property : join.columns) {
                    if (attrToAttr.self().equals(property.name)) {
                        join1 = join;
                        attrToAttrPropertyMap.put(attrToAttr, property);
                    }
                }
            }
        }
        return join1;
    }

    protected void initWithAsCondition(Database.AttrToAttr attrToAttr, SqlBuilder.Property property, SqlBuilder.WithAs.As as, SqlBuilder.Join join) {
        SqlBuilder.Exists exists = as.toExists(as.name, false);
        exists.conditions.add(new SqlBuilder.EqualCondition(property.column, Optional.ofNullable(exists.findProperty(attrToAttr.target())).orElseThrow(() -> new IllegalStateException("没有找到字段:" + attrToAttr.target())).column));
        join.conditions.add(exists);
    }

    protected boolean hasParams(Map<String, Object> params) {
        for (Database.Part part : parts) {
            for (Database.Attr attr : part.attrs()) {
                if (params.containsKey(attr.alias())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void buildQuerySelect(SqlBuilder.SelectGroup selectGroup) {
        Iterator<Database.Part> parts = Arrays.stream(this.parts).iterator();
        Database.Part part;
        if (parent == null) {
            part = parts.next();
            SqlBuilder.SelectGroup.Member member = new SqlBuilder.SelectGroup.Member();
            String idKey = type.getAnnotation(Database.Main.class).id();
            SimpleQuery query = new SimpleQuery(toSelect(part), idKey);
            member.level = SqlBuilder.SelectGroup.Level.Strong;
            member.query = query;
            selectGroup.add(member);

        }
        while (parts.hasNext()) {
            part = parts.next();
            SqlBuilder.SelectGroup.Member member = new SqlBuilder.SelectGroup.Member();
            SimpleQuery query = new SimpleQuery(toSelect(part), part.attrs());
            member.level = SqlBuilder.SelectGroup.Level.Strong;
            member.query = query;
            selectGroup.add(member);
        }
        Iterator<Database.Relation> iterator = Arrays.stream(type.getAnnotationsByType(Database.Relation.class)).iterator();
        while (iterator.hasNext()) {
            Database.Relation relation = iterator.next();
            AnnotationBuildInfo annotationBuildInfo = get(relation.target());
            SqlBuilder.SelectGroup.Member member = new SqlBuilder.SelectGroup.Member();
            SqlBuilder.SelectGroup.Query query = new LazyQuery(annotationBuildInfo, relation);
            member.level = relation.one2More() ? SqlBuilder.SelectGroup.Level.Multi : SqlBuilder.SelectGroup.Level.Weak;
            member.query = query;
            selectGroup.add(member);
        }
    }


    public static <T extends IEntity> List<T> query(Class<T> type, Map<String, Object> params) {
        AnnotationBuildInfo annotationBuildInfo = AnnotationBuildInfo.get(type);
        QueryParameter queryParameter = new QueryParameter(params);
        annotationBuildInfo.buildQuerySelect(queryParameter);
        SqlBuilder.Select select = queryParameter.main;
        System.out.println(select);
        SqlBuilder.SelectGroup selectGroup = new SqlBuilder.SelectGroup();
        annotationBuildInfo.buildQuerySelect(selectGroup);
        System.out.println(selectGroup);
        return List.of();
    }

    public IEntity get(Object id) {
        return null;
    }

    protected void toJoin(Database.Part part, QueryParameter cache, SqlBuilder.Select select) {
        List<Database.Attr> relAttrs = getRelAttr(part, cache);
        if (CollectionUtils.isEmpty(relAttrs)) throw new IllegalStateException("存在未关联的对象" + part.name());
        SqlBuilder.Join join = toJoin(part);
        //添加入参,更新strong
        initParameter(join, cache, part);
        //添加条件
        initCondition(join, relAttrs, cache.colsRelMapper);
        select.joins.add(join);
    }

    protected void initParameter(SqlBuilder.Join join, QueryParameter cache, Database.Part part) {
        join.addInputCondition(cache.condition, cache.prefix);
        cache.addAttrToMapper(part, join);
    }

    protected boolean hasParams(Database.Part part, Map<String, Object> params) {
        for (Database.Attr attr : part.attrs()) {
            if (attr.relAttr().isEmpty()) continue;
            if (params.containsKey(attr.alias())) return true;
        }
        return false;
    }

    protected boolean hasParams(Database.Relation relation, Map<String, Object> params) {
        String key = relation.alias() + ".";
        for (String s : params.keySet()) {
            if (s.startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasParams(Database.Relation2 relation, Map<String, Object> params) {
        String key = relation.alias() + ".";
        for (String s : params.keySet()) {
            if (s.startsWith(key)) {
                return true;
            }
        }
        return false;
    }


    protected void initCondition(SqlBuilder.Join join, List<Database.Attr> attrs, Map<String, SqlBuilder.Column> rel) {
        for (Database.Attr attr : attrs) {
            if (rel.containsKey(attr.relAttr())) {
                join.conditions.add(new SqlBuilder.EqualCondition(join.columns.stream().filter(property -> property.name.equals(attr.alias())).findFirst().orElseThrow(() -> new IllegalStateException("没有摘到字段：" + attr.alias())).column, rel.get(attr.relAttr())));
            }
        }
    }

    protected void initProperties(Database.Part part, SqlBuilder.Join join) {
        for (Database.Attr column : part.attrs()) {
            join.columns.add(new SqlBuilder.Property(new SqlBuilder.BaseColumn(join, column.name()), column.alias(), column.classType()));
        }
    }

    protected SqlBuilder.Join toJoin(Database.Part part) {
        Matcher matcher = readTableInfo(part.name());
        SqlBuilder.Join join = new SqlBuilder.Join(matcher.group(1), matcher.group(2));
        initProperties(part, join);
        return join;
    }

    protected SqlBuilder.Exists toExists(Database.Part part, boolean isNot) {
        Matcher matcher = readTableInfo(part.name());
        SqlBuilder.Exists select = new SqlBuilder.Exists(matcher.group(1), matcher.group(2), isNot);
        initProperties(part, select);
        return select;
    }

    protected SqlBuilder.Select toSelect(Database.Part part) {
        Matcher matcher = readTableInfo(part.name());
        SqlBuilder.Select select = new SqlBuilder.Select(matcher.group(1), matcher.group(2));
        initProperties(part, select);
        return select;
    }


    static Class<? extends IEntity> getMain(Class<? extends IEntity> aClass) {
        if (aClass.getAnnotation(Database.Main.class) != null) {
            return aClass;
        }
        for (Class<?> anInterface : aClass.getInterfaces()) {
            if (IEntity.class.isAssignableFrom(anInterface)) {
                return getMain((Class) anInterface);
            }
        }
        throw new IllegalStateException(aClass + "未定义Database.Main注解");
    }

    static AnnotationBuildInfo get(Class<? extends IEntity> anInterface) {
        Class<? extends IEntity> aClass = getMain(anInterface);
        for (AnnotationBuildInfo info : cache.getOrDefault(aClass, Collections.emptyList())) {
            if (info.type == anInterface) {
                return info;
            }
        }
        synchronized (cache) {
            for (AnnotationBuildInfo info : cache.getOrDefault(aClass, Collections.emptyList())) {
                if (info.type == anInterface) {
                    return info;
                }
            }
            return new AnnotationBuildInfo(anInterface);
        }
    }

    public static class SimpleQuery implements SqlBuilder.SelectGroup.Query {
        public SqlBuilder.Select select;
        Map<String, Object> result;
        public Map<String, SqlBuilder.Property> relCols = new HashMap<>();

        protected SimpleQuery(SqlBuilder.Select select, String idKey) {
            this.select = select;
            relCols.put(idKey, select.columns.stream().filter(property -> property.name.equals(idKey)).findFirst().orElseThrow(() -> new IllegalStateException("没有找到对应的属性:" + idKey)));
        }

        protected SimpleQuery(SqlBuilder.Select select, Database.Attr[] attrs) {
            this.select = select;
            for (Database.Attr attr : attrs) {
                if (attr.relAttr().isEmpty()) continue;
                relCols.put(attr.relAttr(), select.columns.stream().filter(property -> property.name.equals(attr.alias())).findFirst().orElseThrow(() -> new IllegalStateException("没有找到对应的属性:" + attr.alias())));
            }
        }


        @Override
        public boolean propertyMatched(String s) {
            return select.findProperty(s) != null;
        }

        public Object load(String s) {
            return new Object();
        }


    }

    public static class LazyQuery implements SqlBuilder.SelectGroup.Query {
        boolean one2More;
        public String name;
        public AnnotationBuildInfo buildInfo;
        SqlBuilder.SelectGroup group;
        public Map<String, String> relCols = new HashMap<>();

        protected LazyQuery(AnnotationBuildInfo buildInfo, Database.Relation relation) {
            this.buildInfo = buildInfo;
            this.name = relation.alias() + ".";
            this.one2More = relation.one2More();
            for (Database.AttrToAttr attrToAttr : relation.rel()) {
                relCols.put(attrToAttr.self(), attrToAttr.target());
            }
        }

        @Override
        public boolean propertyMatched(String s) {
            return s.startsWith(name);
        }

        public Object load(String s) {
            initGroup();
            for (SqlBuilder.SelectGroup.Member member : group) {
                if (member.query.propertyMatched(s)) {
                    return member.query.load(s);
                }
            }
            return null;
        }

        protected void initGroup() {
            if (group == null) {
                synchronized (this) {
                    if (group == null) {
                        group = new SqlBuilder.SelectGroup();
                        buildInfo.buildQuerySelect(group);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        query(IDemoLinkEntity.class, Map.of("id", 1, "link2Parent.link.code", 3, "link2Children.link.code", 4));
        query(IDemoRfsEntity.class, Map.of("id", 1, "res.entity.code", 3));
    }
}
