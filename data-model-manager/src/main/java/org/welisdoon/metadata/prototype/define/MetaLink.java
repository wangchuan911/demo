package org.welisdoon.metadata.prototype.define;

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
public class MetaLink extends MetaPrototype<MetaLink> implements ISequenceEntity {
    Long objectId;
    MetaObject object;

    Long attributeId;
    MetaObject.Attribute attribute;

    Long instanceId;
    MetaInstance instance;

    Long valueId;
    MetaValue value;

    int sequence;

    LinkMetaType type;

    @Override
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public void setSequence(int i) {
        this.sequence = i;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public MetaObject getObject() {
        if (Objects.nonNull(objectId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(object), metaLink -> object = MetaUtils.getInstance().getObject(getObjectId()));
        return object;
    }

    public void setObject(MetaObject object) {
        this.object = object;
    }


    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public MetaObject.Attribute getAttribute() {
        if (Objects.nonNull(attributeId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(attribute), metaLink -> attribute = MetaUtils.getInstance().getAttribute(getAttributeId()));
        return attribute;
    }

    public void setAttribute(MetaObject.Attribute attribute) {
        this.attribute = attribute;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }


    public MetaInstance getInstance() {
        return instance;
    }

    public void setInstance(MetaInstance instance) {
        this.instance = instance;
    }


    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }


    public MetaValue getValue() {
        if (Objects.nonNull(valueId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(value), metaLink -> value = MetaUtils.getInstance().getValue(getValueId()));
        return value;
    }

    public void setValue(MetaValue value) {
        this.value = value;
    }


    public LinkMetaType getType() {
        return Optional.ofNullable(type).orElseGet(() -> {
            type = LinkMetaType.getInstance(typeId);
            return type;
        });
    }

    @Override
    public List<MetaLink> children() {
        ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(children), metaLink -> children = MetaUtils.getInstance().getChildrenLinks(getId()));
        return super.children();
    }
}
