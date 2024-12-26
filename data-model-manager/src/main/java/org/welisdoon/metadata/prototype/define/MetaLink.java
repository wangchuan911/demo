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
public class MetaLink extends MetaPrototype<MetaLink> implements ISequenceEntity, ITypeEntity<LinkMetaType> {
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
        this.sequence = i;
        return this;
    }

    public Long getObjectId() {
        return objectId;
    }

    public MetaLink setObjectId(Long objectId) {
        this.objectId = objectId;
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaObject getObject() {
        if (Objects.nonNull(objectId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(object), metaLink -> object = MetaUtils.getInstance().getObject(getObjectId()));
        return object;
    }

    public MetaLink setObject(MetaObject object) {
        this.object = object;
        return this;
    }


    public Long getAttributeId() {
        return attributeId;
    }

    public MetaLink setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaObject.Attribute getAttribute() {
        if (Objects.nonNull(attributeId))
            ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(attribute), metaLink -> attribute = MetaUtils.getInstance().getAttribute(getAttributeId()));
        return attribute;
    }

    public MetaLink setAttribute(MetaObject.Attribute attribute) {
        this.attribute = attribute;
        return this;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public MetaLink setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
        return this;
    }


    public MetaInstance getInstance() {
        return instance;
    }

    public MetaLink setInstance(MetaInstance instance) {
        this.instance = instance;
        return this;
    }


    public Long getValueId() {
        return valueId;
    }

    public MetaLink setValueId(Long valueId) {
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
    public List<MetaLink> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaLink -> Objects.nonNull(children), metaLink -> children = MetaUtils.getInstance().getChildrenLinks(getId()));
        return super.getChildren();
    }

    public Long getLinkId() {
        return linkId;
    }

    public MetaLink setLinkId(Long linkId) {
        this.linkId = linkId;
        return this;
    }

    public MetaLink getLink() {
        return link;
    }

    public MetaLink setLink(MetaLink link) {
        this.link = link;
        return this;
    }
}
