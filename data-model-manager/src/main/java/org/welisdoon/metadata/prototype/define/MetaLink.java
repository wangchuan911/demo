package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaLink extends MetaPrototype implements ISequenceEntity, ITypeEntity<LinkMetaType>, MetaPrototype.Child<MetaLink>, MetaPrototype.Parent<MetaLink> {
    Long objectId;
    MetaObject object;

    Long attributeId;
    MetaObject.Attribute attribute;

    Long instanceId;
    MetaInstance instance;

    Long valueId;
    MetaValue value;

    Long linkId;
    MetaLink link;

    int sequence;

    LinkMetaType type;

    List<MetaLink> children;
    MetaLink parent;

    @Override
    public String getName() {
        if (StringUtils.isEmpty(super.getName())) {
            setName(getType().getDesc());
        }
        return super.getName();
    }

    @Override
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public MetaLink setSequence(int i) {
        setEditing(this.sequence, i);
        this.sequence = i;
        return this;
    }

    public Long getObjectId() {
        return objectId;
    }

    public MetaLink setObjectId(Long objectId) {
        setEditing(this.objectId, objectId);
        this.objectId = objectId;
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public <T extends MetaObject> T getObject() {
        if (Objects.nonNull(objectId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(object), metaLink -> object = MetaUtils.getInstance().getObject(getObjectId()));
        return (T) object;
    }

    public MetaLink setObject(MetaObject object) {
        this.object = object;
        setObjectId(this.object == null ? null : this.object.getId());
        return this;
    }


    public Long getAttributeId() {
        return attributeId;
    }

    public MetaLink setAttributeId(Long attributeId) {
        setEditing(this.attributeId, attributeId);
        this.attributeId = attributeId;
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public <T extends MetaObject.Attribute> T getAttribute() {
        if (Objects.nonNull(attributeId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(attribute), metaLink -> attribute = MetaUtils.getInstance().getAttribute(getAttributeId()));
        return (T) attribute;
    }

    public MetaLink setAttribute(MetaObject.Attribute attribute) {
        this.attribute = attribute;
        setAttributeId(this.attribute == null ? null : this.attribute.getId());
        return this;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public MetaLink setInstanceId(Long instanceId) {
        setEditing(this.instanceId, instanceId);
        this.instanceId = instanceId;
        return this;
    }


    public <T extends MetaInstance> T getInstance() {
        return (T) instance;
    }

    public MetaLink setInstance(MetaInstance instance) {
        this.instance = instance;
        setInstanceId(this.instance == null ? null : instance.getId());
        return this;
    }


    public Long getValueId() {
        return valueId;
    }

    public MetaLink setValueId(Long valueId) {
        setEditing(this.valueId, valueId);
        this.valueId = valueId;
        return this;
    }


    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaValue getValue() {
        if (Objects.nonNull(valueId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(value), metaLink -> value = MetaUtils.getInstance().getValue(getValueId()));
        return value;
    }

    public MetaLink setValue(MetaValue value) {
        setEditing(this.value, value);
        this.value = value;
        return this;
    }


    public LinkMetaType getType() {
        return Optional.ofNullable(type).orElseGet(() -> {
            type = LinkMetaType.getInstance(typeId);
            return type;
        });
    }

    @Override
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaLink getParent() {
        if (Objects.nonNull(getParentId()))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(parent), metaLink -> parent = MetaUtils.getInstance().getMetaLinkDao().get(getParentId()));
        return parent;
    }

    @Override
    public MetaLink setParent(MetaLink parent) {
        this.parent = parent;
        setParentId(this.parent == null ? null : parent.getId());
        return this;
    }

    @Override
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<MetaLink> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(children), metaLink -> children = MetaUtils.getInstance().getChildrenLinks(getId()));
        return children;
    }

    @Override
    public MetaLink setChildren(List<MetaLink> children) {
        this.children = children;
        if (children != null)
            this.children.forEach(this::bind);
        return this;
    }

    public Long getLinkId() {
        return linkId;
    }

    public MetaLink setLinkId(Long linkId) {
        setEditing(this.linkId, linkId);
        this.linkId = linkId;
        return this;
    }

    public MetaLink getLink() {
        return link;
    }

    public MetaLink setLink(MetaLink link) {
        this.link = link;
        setLinkId(this.link == null ? null : this.link.getId());
        return this;
    }

    @Override
    public int remove() {
        super.remove();
        int update = 0;
        if (getParentId() != null) {
            update += getChildren().stream().map(MetaLink::remove).reduce(0, Integer::sum);
        }
        update += MetaUtils.getInstance().getMetaLinkDao().delete(this.getId());
        return update;
    }

    @Override
    public int save() {
        if (!isEditing())
            return 0;

        int update;
        if (getId() != null) {
            update = MetaUtils.getInstance().getMetaLinkDao().put(this);
        } else {
            super.save();
            update = MetaUtils.getInstance().getMetaLinkDao().add(this);
        }
        setState(LifeState.Save);
        for (MetaLink child : getChildren()) {
            child.setParentId(this.id);
            update += child.save();
        }

        if (this.object != null) {
            object.save();
            objectId = object.getId();
        }
        if (this.attribute != null) {
            attribute.save();
            attributeId = attribute.getId();
        }
        if (this.value != null) {
            value.save();
            valueId = value.getId();
        }

        if (this.instance != null) {
            instance.save();
            instanceId = instance.getId();
        }
        if (this.link != null) {
            link.save();
            linkId = link.getId();
        }
        return update;
    }

}
