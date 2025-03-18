package org.welisdoon.metadata.prototype.define;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    LifeState state = LifeState.Edit;

    public Long getId() {
        return id;
    }

    public <T extends MetaPrototype> T setId(Long id) {
        this.setEditing(this.id, id);
        this.id = id;
        return (T) this;
    }

    public Long getTypeId() {
        return typeId;
    }

    public <T extends MetaPrototype> T setTypeId(Long typeId) {
        this.setEditing(this.typeId, typeId);
        this.typeId = typeId;
        return (T) this;
    }

    public Long getParentId() {
        return parentId;
    }

    public <T extends MetaPrototype> T setParentId(Long parentId) {
        this.setEditing(this.parentId, parentId);
        this.parentId = parentId;
        return (T) this;
    }

    public String getCode() {
        return code;
    }

    public <T extends MetaPrototype> T setCode(String code) {
        this.setEditing(this.code, code);
        this.code = code;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public <T extends MetaPrototype> T setName(String name) {
        this.setEditing(this.name, name);
        this.name = name;
        return (T) this;
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

    public boolean isEditing() {
        return this.state == LifeState.Edit;
    }

    public boolean isDelete() {
        return this.state == LifeState.Delete;
    }


    public LifeState getState() {
        return this.state;
    }

    protected void setState(LifeState state) {
        if (this.state == LifeState.Readonly) {
            return;
        }
        this.state = state;
    }

    protected <T> void setEditing(T oldVal, T newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            setState(LifeState.Edit);
        }
    }

    public interface Parent<T> {
        List<T> getChildren();

        default <P extends Parent> P setChildren(List<T> children) {
            List<T> list = getChildren();
            list.clear();
            if (children != null) {
                list.addAll(children);
                list.forEach(this::bind);
            }
            return (P) this;
        }

        default void bind(T child) {
            if (child instanceof Child && this instanceof Parent) {
                ((Child) child).setParent(this);
            }
        }

        default <P extends Parent> P addChildren(Stream<T> children) {
            if (children == null) return (P) this;
            List<T> list = getChildren();
            children.forEach(t -> {
                list.add(t);
                this.bind(t);
            });
            return (P) this;
        }
    }


    public interface Child<T> {
        T getParent();

        Child setParent(T parent);

    }

    public enum LifeState {
        Edit, Save, Delete, Readonly
    }
}
