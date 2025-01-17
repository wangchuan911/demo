package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @Classname MetaPrototype
 * @Description TODO
 * @Author Septem
 * @Date 11:48
 */
public abstract class MetaPrototype {
    Long id, typeId, parentId;
    String code, name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public <T extends MetaPrototype> T setTypeId(Long typeId) {
        this.typeId = typeId;
        return (T) this;
    }

    public Long getParentId() {
        return parentId;
    }

    public <T extends MetaPrototype> T setParentId(Long parentId) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaPrototype that = (MetaPrototype) o;
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
        this.typeId = metaPrototype.typeId;
        this.parentId = metaPrototype.parentId;
    }

    interface Parent<T> {
        List<T> getChildren();

        Parent setChildren(List<T> children);

        default void bind(T child) {
            if (child instanceof Child && this instanceof Parent) {
                ((Child) child).setParent(this);
            }
        }

        default Parent addChildren(Stream<T> children) {
            if (children == null) return this;
            if (getChildren() == null) {
                setChildren(new LinkedList<>());
            }
            children.forEach(t -> getChildren().add(t));
            return this;
        }
    }

    interface Child<T> {
        T getParent();

        Child setParent(T parent);

    }
}
