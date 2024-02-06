package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaLink extends MetaPrototype implements ISequenceEntity {
    Long relObjectId, objectId;
    MetaObject relObject, object;

    Long relAttributeId, attributeId;
    MetaObject.Attribute relAttribute, attribute;


    Long instanceId, relInstanceId;
    MetaInstance instance, relInstance;

    Long valueId, relValueId;
    MetaKeyValue value, relValue;

    int sequence;

    Long relLinkId;
    MetaLink relLink;

    @Override
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public void setSequence(int i) {
        this.sequence = i;
    }

    public Long getRelObjectId() {
        return relObjectId;
    }

    public void setRelObjectId(Long relObjectId) {
        this.relObjectId = relObjectId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public MetaObject getRelObject() {
        return relObject;
    }

    public void setRelObject(MetaObject relObject) {
        this.relObject = relObject;
    }

    public MetaObject getObject() {
        return object;
    }

    public void setObject(MetaObject object) {
        this.object = object;
    }

    public Long getRelAttributeId() {
        return relAttributeId;
    }

    public void setRelAttributeId(Long relAttributeId) {
        this.relAttributeId = relAttributeId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public MetaObject.Attribute getRelAttribute() {
        return relAttribute;
    }

    public void setRelAttribute(MetaObject.Attribute relAttribute) {
        this.relAttribute = relAttribute;
    }

    public MetaObject.Attribute getAttribute() {
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

    public Long getRelInstanceId() {
        return relInstanceId;
    }

    public void setRelInstanceId(Long relInstanceId) {
        this.relInstanceId = relInstanceId;
    }

    public MetaInstance getInstance() {
        return instance;
    }

    public void setInstance(MetaInstance instance) {
        this.instance = instance;
    }

    public MetaInstance getRelInstance() {
        return relInstance;
    }

    public void setRelInstance(MetaInstance relInstance) {
        this.relInstance = relInstance;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getRelValueId() {
        return relValueId;
    }

    public void setRelValueId(Long relValueId) {
        this.relValueId = relValueId;
    }

    public MetaKeyValue getValue() {
        return value;
    }

    public void setValue(MetaKeyValue value) {
        this.value = value;
    }

    public MetaKeyValue getRelValue() {
        return relValue;
    }

    public void setRelValue(MetaKeyValue relValue) {
        this.relValue = relValue;
    }

    public Long getRelLinkId() {
        return relLinkId;
    }

    public void setRelLinkId(Long relLinkId) {
        this.relLinkId = relLinkId;
    }

    public MetaLink getRelLink() {
        return relLink;
    }

    public void setRelLink(MetaLink relLink) {
        this.relLink = relLink;
    }


}
