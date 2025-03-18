package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.*;

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
    MetaProtoList<MetaObject> children = new MetaProtoList<>();
    MetaObject parent;

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<Attribute> getAttributes() {
        return Optional.ofNullable(attributes).orElseGet(() ->
                attributes = new MetaProtoList<>(MetaUtils.getInstance().getMetaAttributeDao().list(new Attribute().setObjectId(this.getId())))
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

    public MetaObject setConstruct(MetaLink construct) {
        this.construct = construct;
        this.setConstructId(construct.getId());
        return this;
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
    public MetaProtoList<MetaObject> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaLink -> children.getState() != MetaProtoList.LifeState.Initial, metaLink -> {
                    children.setState(MetaProtoList.LifeState.Loading);
                    setChildren(MetaUtils.getInstance().getMetaObjectDao().list(new MetaObjectCondition().setParentId(this.getId())));
                    children.setState(MetaProtoList.LifeState.Loaded);
                }
        );
        return children;
    }

    @Override
    public int remove() {
        if (this.isDelete())
            return 0;
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

        int update = 0;
        if (isEditing() && getId() == null) {
            super.save();
            update += MetaUtils.getInstance().getMetaObjectDao().add(this);
            setState(LifeState.Save);
        }
        for (Attribute attribute : getAttributes()) {
            attribute.setObjectId(this.getId());
            update += attribute.save();
        }
        getConstruct().setObjectId(this.getId());
        update += getConstruct().save();
        setConstructId(getConstruct().getId());
        if (isEditing()) {
            MetaUtils.getInstance().getMetaObjectDao().put(this);
            setState(LifeState.Save);
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

        @JsonIgnore
        @JSONField(deserialize = false, serialize = false)
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
            if (this.isDelete())
                return 0;
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
            int update = 0;
            getParent().save();
            setParentId(getParent().getId());
            if (isEditing()) {
                if (getId() != null) {
                    update = MetaUtils.getInstance().getMetaAttributeDao().put(this);
                } else {
                    super.save();
                    update = MetaUtils.getInstance().getMetaAttributeDao().add(this);
                }
                setState(LifeState.Save);
            }
            return update;
        }

        public Attribute setParent(MetaLink parent) {
            this.parent = parent;
            setParentId(this.parent == null ? null : this.parent.getId());
            return this;
        }

        @JsonIgnore
        @JSONField(deserialize = false, serialize = false)
        public MetaLink getParent() {
            ObjectUtils.synchronizedInitial(this, metaLink -> this.parent != null, metaLink -> {
                if (this.parentId == null) {
                    parent = new MetaLink().setTypeId(LinkMetaType.AttrConstructor.getId());
                } else {
                    parent = MetaUtils.getInstance().getMetaLinkDao().get(getParentId());
                }
                setParent(parent);
            });
            return this.parent;
        }
    }
}
