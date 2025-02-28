package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaObject extends MetaPrototype implements ITypeEntity<ObjectMetaType>, Construction, MetaPrototype.Child<MetaObject>, MetaPrototype.Parent<MetaObject> {
    List<Attribute> attributes;
    ObjectMetaType type;
    Long constructId;
    MetaLink construct;
    MetaList<MetaObject> children;
    MetaObject parent;

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<Attribute> getAttributes() {
        return Optional.ofNullable(attributes).orElseGet(() ->
                attributes = MetaUtils.getInstance().getMetaAttributeDao().list(new Attribute().setObjectId(this.getId()))
        );
    }

    public ObjectMetaType getType() {
        return Optional.ofNullable(type).orElseGet(() -> {
            type = ObjectMetaType.getInstance(typeId);
            return type;
        });
    }

    public Long getConstructId() {
        return constructId;
    }

    public MetaObject setConstructId(Long constructId) {
        setEditing(this.constructId, constructId);
        this.constructId = constructId;
        return this;
    }

    @Override
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaLink getConstruct() {
        if (getConstructId() == null)
            return null;
        return Optional.ofNullable(construct).orElseGet(() ->
                construct = MetaUtils.getInstance().getMetaLinkDao().get(getConstructId())
        );
    }

    @Override
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaObject getParent() {
        if (Objects.isNull(parentId))
            return null;
        ObjectUtils.synchronizedInitial(this,
                tAttribute -> Objects.nonNull(parent),
                tAttribute ->
                        parent = MetaUtils.getInstance().getObject(parentId)
        );
        return parent;
    }

    @Override
    public MetaObject setParent(MetaObject parent) {
        this.parent = parent;
        setParentId(this.parent == null ? null : parent.getId());
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaList<MetaObject> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(children), metaLink ->
                setChildren((MetaList) MetaUtils.getInstance().getMetaObjectDao().list(new MetaObjectCondition().setParentId(this.getId())))
        );
        return children;
    }

    @Override
    public MetaObject setChildren(MetaList<MetaObject> children) {
        this.children = children;
        if (children != null)
            this.children.forEach(this::bind);
        return this;
    }

    @Override
    public int remove() {
        int update = getChildren().stream().map(MetaObject::remove).reduce(0, Integer::sum);
        super.remove();
        if (getConstructId() != null) {
            update += MetaUtils.getInstance().getMetaLinkDao().get(getConstructId()).remove();
        }
        setState(LifeState.Delete);
        return update;
    }

    @Override
    public int save() {
        if (!isEditing())
            return 0;

        int update;
        if (getId() != null) {
            update = MetaUtils.getInstance().getMetaObjectDao().put(this);
        } else {
            super.save();
            update = MetaUtils.getInstance().getMetaObjectDao().add(this);
        }
        setState(LifeState.Save);
        for (Attribute attribute : getAttributes()) {
            attribute.setObjectId(this.getId());
            update += attribute.save();
        }
        return update;
    }

    /**
     * @Classname MetaObject
     * @Description TODO
     * @Author Septem
     * @Date 11:41
     */
    public static class Attribute extends MetaPrototype implements ITypeEntity<AttributeMetaType> {
        Long objectId;
        AttributeMetaType type;
        MetaLink parent;

        public Long getObjectId() {
            return objectId;
        }

        public Attribute setObjectId(Long objectId) {
            setEditing(this.objectId, objectId);
            this.objectId = objectId;
            return this;
        }

        public MetaObject getObject() {
            return MetaUtils.getInstance().getObject(id);
        }

        public AttributeMetaType getType() {
            return Optional.ofNullable(type).orElseGet(() -> {
                type = AttributeMetaType.getInstance(typeId);
                return type;
            });
        }

        @Override
        public int remove() {
            super.remove();
            int update = MetaUtils.getInstance().getMetaObjectDao().delete(this.getId());
            if (getParentId() != null) {
                update += MetaUtils.getInstance().getMetaLinkDao().get(getParentId()).remove();
            }
            setState(LifeState.Delete);
            return update;
        }

        @Override
        public int save() {
            if (!isEditing())
                return 0;
            int update;
            if (getId() != null) {
                update = MetaUtils.getInstance().getMetaAttributeDao().put(this);
                setState(LifeState.Save);
                return update;
            }
            super.save();
            update = MetaUtils.getInstance().getMetaAttributeDao().add(this);
            setState(LifeState.Save);
            return update;
        }

        public Attribute setParent(MetaLink parent) {
            this.parent = parent;
            setParentId(this.parent == null ? null : this.parent.getId());
            return this;
        }

        public MetaLink getParent() {
            ObjectUtils.synchronizedInitial(this, metaLink -> this.parent != null || Objects.nonNull(getParentId()), metaLink ->
                    setParent(MetaUtils.getInstance().getMetaLinkDao().get(getParentId()))
            );
            return this.parent;
        }
    }
}
