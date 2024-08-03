package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Classname MetaPrototype
 * @Description TODO
 * @Author Septem
 * @Date 11:48
 */
public abstract class MetaPrototype<T extends MetaPrototype> {
    Long id, typeId, parentId;
    String code, name;
    T parent;
    List<T> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public T setTypeId(Long typeId) {
        this.typeId = typeId;
        return (T) this;
    }

    public Long getParentId() {
        return parentId;
    }

    public T setParentId(Long parentId) {
        this.parentId = parentId;
        return (T) this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(T parent) {
        this.parent = parent;
        if (Objects.isNull(this.parent.children)) {
            synchronized (this.parent) {
                if (Objects.isNull(this.parent.children)) {
                    this.parent.children = new LinkedList<>();
                }
            }
        }
        this.parent.children.add(this);
    }

    @JsonIgnore
    @JSONField(deserialize = false)
    public T parent() {
        return parent;
    }

    public void setChildren(List<T> children) {
        this.children = children;
        if (CollectionUtils.isEmpty(children)) return;
        this.children.forEach(t -> t.setParent(this));
    }

    public void addChildren(T... children) {
        if (Objects.isNull(this.children))
            synchronized (this) {
                if (Objects.isNull(this.children))
                    this.children = new LinkedList<>();
            }

        for (T child : children) {
            child.setParent(this);
            this.children.add(child);
        }
    }

    public List<T> children() {
        return children;
    }

    public final List<T> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaPrototype<?> that = (MetaPrototype<?>) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getTypeId(), that.getTypeId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTypeId());
    }

    public void copyTo(MetaPrototype metaPrototype) {
        this.id = metaPrototype.id;
        this.code = metaPrototype.code;
        this.name = metaPrototype.name;
        this.parent = (T) metaPrototype.parent;
        this.typeId = metaPrototype.typeId;
        this.parentId = metaPrototype.parentId;
        this.children = metaPrototype.children;
    }
}
