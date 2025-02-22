package org.welisdoon.metadata.prototype.define;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
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


    private static Map<Class<? extends MetaPrototype>, Deque<MetaPrototype>> cache = new ConcurrentHashMap<>();

    public int remove() {
        if (!cache.containsKey(this)) {
            synchronized (cache) {
                if (!cache.containsKey(this)) {
                    cache.put(this.getClass(), new LinkedBlockingDeque<>());
                }
            }
        }
        cache.get(this.getClass()).offerFirst(this);
        return 0;
    }

    public int save() {
        if (this.getId() != null) {
            return 0;
        }
        if (cache.get(this.getClass()) != null) {
            synchronized (cache) {
                MetaPrototype temp = cache.get(this.getClass()).pollFirst();
                if (temp != null) {
                    this.setId(temp.getId());
                }
            }
        }
        return 0;
    }

    public interface Parent<T> {
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


    public interface Child<T> {
        T getParent();

        Child setParent(T parent);

    }
}
