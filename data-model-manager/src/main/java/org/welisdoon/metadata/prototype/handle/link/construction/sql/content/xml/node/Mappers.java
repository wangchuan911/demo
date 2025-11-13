package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.SqlParameter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname Mappers
 * @Description TODO
 * @Author Septem
 * @Date 9:27
 */
public class Mappers extends LikeMyBatisSqlNode {
    public Mappers(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
        super(parent, attributes);
    }

    public Mappers() {
        this(null, Map.of());
    }

    public static abstract class SqlExecuteNode<R> extends LikeMyBatisSqlNode implements Script<SqlParameter> {

        public SqlExecuteNode(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }

        @Override
        public String getScript(SqlParameter request, String split) {
            return getChild(likeMyBatisSqlNode -> likeMyBatisSqlNode instanceof Script).stream().map(likeMyBatisSqlNode -> ((Script) likeMyBatisSqlNode).getScript(request, split)).collect(Collectors.joining(split));
        }

        abstract public R execute(SqlParameter request);

        @Tag(value = "if", parentTagTypes = BaseUnit.class, desc = "单条条件判断")
        @Attr(name = "test", desc = "判断条件")
        public static class If extends LikeMyBatisSqlNode implements Script<SqlParameter> {
            public If(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                super(parent, attributes);
            }

            protected boolean matched(SqlParameter sqlParameter) {
                return OgnlUtils.getValue(attributes.get("test"), sqlParameter.getOgnlContext(), sqlParameter.getBus(), Boolean.class);
            }

            @Override
            public String getScript(SqlParameter request, String split) {
                return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split));
            }
        }

        @Tag(value = "choice", parentTagTypes = BaseUnit.class, desc = "多条件判断")
        public static class Choice extends LikeMyBatisSqlNode implements Script<SqlParameter> {

            public Choice(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                super(parent, attributes);
            }

            @Override
            public String getScript(SqlParameter request, String split) {
                for (LikeMyBatisSqlNode child : getChildren()) {
                    if (child instanceof When && ((When) child).matched(request)) {
                        return ((When) child).getScript(request, split);
                    } else if (child instanceof Otherwise) {
                        return ((Otherwise) child).getScript(request, split);
                    }
                }
                return "";
            }

            @Tag(value = "when", parentTagTypes = BaseUnit.class, desc = "多条件判断（else if）")
            @Attr(name = "test", desc = "判断条件")
            public static class When extends If {
                public When(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                    super(parent, attributes);
                }
            }

            @Tag(value = "otherwise", parentTagTypes = BaseUnit.class, desc = "多条件判断（else）")
            public static class Otherwise extends LikeMyBatisSqlNode implements Script<SqlParameter> {

                public Otherwise(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                    super(parent, attributes);
                }

                @Override
                public String getScript(SqlParameter request, String split) {
                    return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split));
                }
            }
        }

        @Tag(value = "foreach", parentTagTypes = BaseUnit.class, desc = "循环")
        @Attr(name = "test", desc = "判断条件")
        public static class Foreach extends LikeMyBatisSqlNode implements Script<SqlParameter> {

            public Foreach(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                super(parent, attributes);
            }

            @Override
            public String getScript(SqlParameter request, String split) {
                Object o = OgnlUtils.getValue(attributes.get("collection"), request.getOgnlContext(), request.getBus(), Object.class);
                Stream<?> stream;
                if (o == null) {
                    return "";
                } else if (o.getClass().isArray()) {
                    stream = Arrays.stream((Object[]) o);
                } else if (o instanceof Collection) {
                    stream = ((Collection<?>) o).stream();
                } else if (o instanceof Map) {
                    stream = ((Map<?, ?>) o).entrySet().stream();
                } else {
                    throw new IllegalStateException("不支持的类型" + o.getClass());
                }
                return backupValueAndDo(() -> {
                    return stream.map(o1 -> {
                        try {
                            request.getBus().put(attributes.get("item"), o1);
                            return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split));
                        } finally {
                            request.getBus().remove("item");
                        }
                    }).collect(Collectors.joining(attributes.getOrDefault("split", "")));
                }, request, List.of(attributes.get("item"), attributes.get("index")));
            }
        }

        @Tag(value = "include", parentTagTypes = Mappers.Mapper.class)
        public static class Include extends LikeMyBatisSqlNode implements Script<SqlParameter> {

            public Include(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                super(parent, attributes);
            }

            @Override
            public String getScript(SqlParameter request, String split) {
                Mappers.Mapper mapper = getParent(Root.class::isAssignableFrom);
                List<Property> list = getChild(Property.class);
                return backupValueAndDo(() -> {
                    for (Property property : list) {
                        request.getBus().put(property.attributes.get("name"), property.attributes.get("value"));
                    }
                    return mapper.getSql(attributes.get("refid")).getScript(request, split, this);
                }, request, list.stream().map(property -> property.attributes.get("name")).collect(Collectors.toList()));

            }

            @Tag(value = "property", parentTagTypes = Include.class)
            public static class Property extends LikeMyBatisSqlNode {

                public Property(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
                    super(parent, attributes);
                }
            }
        }
    }

    @Tag(value = "mapper", parentTagTypes = Root.class)
    public static class Mapper extends LikeMyBatisSqlNode implements Root, Executable {
        Map<String, Sql> sqlMap = new HashMap<>();

        Sql getSql(String id) {
            return sqlMap.get(id);
        }

        @Override
        public String getId() {
            return attributes.get("namespace");
        }

        public Mapper(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }


    }

    @Tag(value = "sql", parentTagTypes = Mappers.Mapper.class)
    public static class Sql extends LikeMyBatisSqlNode implements Script<SqlParameter> {
        static ThreadLocal<SqlExecuteNode.Include> local = new InheritableThreadLocal<>();

        public Sql(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            this(parent, attributes, false);
        }

        public Sql(LikeMyBatisSqlNode parent, Map<String, String> attributes, boolean copy) {
            super(parent, attributes);
            if (!copy) {
                ((Mappers.Mapper) parent).sqlMap.put(getId(), this);
            }
        }

        @Override
        public LikeMyBatisSqlNode getParent() {
            return local.get();
        }

        public String getScript(SqlParameter request, String split, SqlExecuteNode.Include include) {
            try {
                local.set(include);
                return getScript(request, split);
            } finally {
                local.remove();
            }
        }
    }

    @Tag(value = "select", parentTagTypes = Mappers.Mapper.class)
    public static class Select extends SqlExecuteNode<List> implements Script<SqlParameter> {

        public Select(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }

        @Override
        public List execute(SqlParameter request) {
            String sql = getScript(request, " ");
            return null;
        }


    }

    @Tag(value = "update", parentTagTypes = Mappers.Mapper.class)
    public static class Update extends SqlExecuteNode<Insert> implements Script<SqlParameter> {

        public Update(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }

        @Override
        public Insert execute(SqlParameter request) {
            String sql = getScript(request, " ");
            ApplicationContextProvider.getApplicationContext().getBean("");
            return null;
        }
    }

    @Tag(value = "insert", parentTagTypes = Mappers.Mapper.class)
    public static class Insert extends Update {
        public Insert(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }
    }

    @Tag(value = "delete", parentTagTypes = Mappers.Mapper.class)
    public static class Delete extends Update {
        public Delete(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
            super(parent, attributes);
        }
    }


    public void select(String id, Map<String, Object> params) {
        for (Mapper child : getChild(Mapper.class)) {
            if (id.startsWith(child.getId())) {
                child.getChild(Select.class);
            }
        }
    }
}
