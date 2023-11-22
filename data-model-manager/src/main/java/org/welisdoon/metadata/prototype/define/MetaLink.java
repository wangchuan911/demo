package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaLink extends MetaPrototype implements ISequenceEntity {
    Long superObjectId, objectId;
    MetaObject superObject, object;

    Long superAttributeId, attributeId;
    MetaAttribute superAttribute, attribute;

    Long parentLinkId;
    MetaLink parentLink;

    Long instanceId, superInstanceId;
    MetaInstance instance, superInstance;

    Long valueId, superValueId;
    MetaKeyValue value, superValue;

    int sequence;

    @Override
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public void setSequence(int i) {
        this.sequence = i;
    }

    public Long getSuperObjectId() {
        return superObjectId;
    }

    public void setSuperObjectId(Long superObjectId) {
        this.superObjectId = superObjectId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public MetaObject getSuperObject() {
        return superObject;
    }

    public void setSuperObject(MetaObject superObject) {
        this.superObject = superObject;
    }

    public MetaObject getObject() {
        return object;
    }

    public void setObject(MetaObject object) {
        this.object = object;
    }

    public Long getSuperAttributeId() {
        return superAttributeId;
    }

    public void setSuperAttributeId(Long superAttributeId) {
        this.superAttributeId = superAttributeId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public MetaAttribute getSuperAttribute() {
        return superAttribute;
    }

    public void setSuperAttribute(MetaAttribute superAttribute) {
        this.superAttribute = superAttribute;
    }

    public MetaAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(MetaAttribute attribute) {
        this.attribute = attribute;
    }

    public Long getParentLinkId() {
        return parentLinkId;
    }

    public void setParentLinkId(Long parentLinkId) {
        this.parentLinkId = parentLinkId;
    }

    public MetaLink getParentLink() {
        return parentLink;
    }

    public void setParentLink(MetaLink parentLink) {
        this.parentLink = parentLink;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getSuperInstanceId() {
        return superInstanceId;
    }

    public void setSuperInstanceId(Long superInstanceId) {
        this.superInstanceId = superInstanceId;
    }

    public MetaInstance getInstance() {
        return instance;
    }

    public void setInstance(MetaInstance instance) {
        this.instance = instance;
    }

    public MetaInstance getSuperInstance() {
        return superInstance;
    }

    public void setSuperInstance(MetaInstance superInstance) {
        this.superInstance = superInstance;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getSuperValueId() {
        return superValueId;
    }

    public void setSuperValueId(Long superValueId) {
        this.superValueId = superValueId;
    }

    public MetaKeyValue getValue() {
        return value;
    }

    public void setValue(MetaKeyValue value) {
        this.value = value;
    }

    public MetaKeyValue getSuperValue() {
        return superValue;
    }

    public void setSuperValue(MetaKeyValue superValue) {
        this.superValue = superValue;
    }
}
