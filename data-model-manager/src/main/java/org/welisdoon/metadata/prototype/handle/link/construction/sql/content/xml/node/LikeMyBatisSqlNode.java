package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node;

import org.apache.commons.lang.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.SqlParameter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Classname Node
 * @Description TODO
 * @Author Septem
 * @Date 10:18
 */
public class LikeMyBatisSqlNode implements BaseUnit<LikeMyBatisSqlNode>, MetaPrototype.Parent<LikeMyBatisSqlNode>, MetaPrototype.Child<LikeMyBatisSqlNode> {
    Map<String, String> attributes;
    List<LikeMyBatisSqlNode> children;
    LikeMyBatisSqlNode parent;

    public LikeMyBatisSqlNode(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
        this.attributes = attributes;
        this.children = new LinkedList<>();
        Arrays.stream(this.getClass().getAnnotations()).filter(annotation -> annotation instanceof Attr).map(annotation -> (Attr) annotation).forEach(attr -> {
            if (attr.options().length > 0 && attr.defaultOption() >= 0 && !this.attributes.containsKey(attr.name())) {
                this.attributes.put(attr.name(), attr.options()[attr.defaultOption()]);
            }
        });
        bindParent(parent);
    }

    @Override
    public String getId() {
        return attributes.get("id");
    }


    @Override
    public <T extends LikeMyBatisSqlNode> List<T> getChild(Predicate<LikeMyBatisSqlNode> predicate) {
        return (List) getChildren().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public <T extends LikeMyBatisSqlNode> List<T> getChildren(Class<T> tClass) {
        List<T> units = new LinkedList<>();
        units.addAll(getChild(tClass));
        for (BaseUnit child : children) {
            units.addAll(child.getChildren(tClass));
        }
        return units;
    }

    @Override
    public <T extends LikeMyBatisSqlNode> T getParent(Class<T> tClass) {
        return getParent(aClass -> aClass == tClass);
    }

    @Override
    public <T extends LikeMyBatisSqlNode> List<T> getParents(Class<T> tClass) {
        return getParents(aClass -> aClass == tClass);
    }

    @Override
    public <T extends LikeMyBatisSqlNode> T getParent(Predicate<Class<?>> predicate) {
        Class<? extends LikeMyBatisSqlNode> pClass;
        LikeMyBatisSqlNode target = this;
        do {
            target = target.getParent();
            if (target == null) break;
            pClass = target.getClass();
        } while (!predicate.test(pClass));
        return (T) target;
    }

    @Override
    public <T extends LikeMyBatisSqlNode> List<T> getParents(Predicate<Class<?>> predicate) {
        List<T> list = new LinkedList<>();
        LikeMyBatisSqlNode t = this;
        while ((t = t.getParent(predicate)) != null) {
            list.add((T) t);
        }
        return list;
    }

    public LikeMyBatisSqlNode copyTo(LikeMyBatisSqlNode parent) {
        try {
            LikeMyBatisSqlNode likeMyBatisSqlNode = this.getClass().getConstructor(LikeMyBatisSqlNode.class, Map.class).newInstance(parent, attributes);
            for (LikeMyBatisSqlNode child : likeMyBatisSqlNode.getChildren()) {
                child.copyTo(likeMyBatisSqlNode);
            }
            return likeMyBatisSqlNode;
        } catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<LikeMyBatisSqlNode> getChildren() {
        return children;
    }

    @Override
    public LikeMyBatisSqlNode getParent() {
        return parent;
    }

    @Override
    public MetaPrototype.Child setParent(LikeMyBatisSqlNode parent) {
        this.parent = parent;
        return this;
    }

    protected <T> T backupValueAndDo(Supplier<T> supplier, SqlParameter sqlParameter, List<String> names) {
        Map<String, Object> objectMap = Map.ofEntries(names.stream().filter(StringUtils::isNotEmpty).map(s -> Map.of(s, OgnlUtils.getValue(s, sqlParameter, Object.class))).toArray(Map.Entry[]::new));
        try {
            return supplier.get();
        } finally {
            objectMap.forEach((s, o) -> {
                sqlParameter.getBus().put(s, o);
            });
        }

    }
}
