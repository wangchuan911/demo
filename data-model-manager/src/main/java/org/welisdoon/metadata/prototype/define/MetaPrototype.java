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
        return this.state == LifeState.Delete || isReadonly();
    }

    public boolean isReadonly() {
        return this.state == LifeState.Readonly;
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
            /*List<T> list = getChildren();
            list.clear();
            if (children != null) {
                list.addAll(children);
                list.forEach(this::bind);
            }*/
            getChildren().clear();
            this.bind(children == null ? Stream.of() : children.stream());
            return (P) this;
        }

        default void bind(Stream<T> children) {
            List<T> list = getChildren();
            if (children == null) {
                return;
            }
            children.forEach(child -> {
                if (child instanceof Child) {
                    ((Child) child).setParent(this);
                }
                list.add(child);
            });
        }

        default <P extends Parent> P addChildren(T... children) {
            this.bind(Arrays.stream(children));
            return (P) this;
        }

        default <P extends Parent> P addChildren(List<T> children) {
            this.bind(children == null ? Stream.of() : children.stream());
            return (P) this;
        }
    }


    public interface Child<T> {
        T getParent();

        Child setParent(T parent);

        default Child bindParent(T parent) {
            setParent(parent);
            if (parent instanceof Parent) {
                Parent<Child> tParent = (Parent) parent;
                tParent.getChildren().add(this);
            }
            return this;
        }
    }

    public enum LifeState {
        Edit, Save, Delete, Readonly
    }
}
