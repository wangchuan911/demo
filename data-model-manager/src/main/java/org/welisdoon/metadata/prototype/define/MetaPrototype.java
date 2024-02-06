package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Classname MetaPrototype
 * @Description TODO
 * @Author Septem
 * @Date 11:48
 */
public abstract class MetaPrototype {
    Long id,
            typeId, parentId;
    String code, name;
    MetaPrototype parent;
    List<MetaPrototype> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public void setParent(MetaPrototype parent) {
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
    public <T extends MetaPrototype> T getParent() {
        return (T) parent;
    }

    public void setChildren(List<MetaPrototype> children) {
        this.children = children;
    }

    public void addChildren(MetaPrototype... children) {
        for (MetaPrototype child : children) {
            child.setParent(this);
        }
    }

    public <T extends MetaPrototype> List<T> getChildren() {
        return (List) children;
    }
}
