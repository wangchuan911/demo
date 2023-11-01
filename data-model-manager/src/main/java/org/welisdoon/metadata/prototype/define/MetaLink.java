package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaLink extends MetaPrototype implements ISequneceEntity {
    Long superMetaObjectId, superObjectTypeId;
    MetaPrototype superMetaObject;

    Long superObjectAttrId, superObjectAttrTypeId;
    MetaPrototype superObjectAttr;

    Long metaObjectId, objectTypeId;
    MetaPrototype metaObject;

    Long objectAttrId, objectAttrTypeId;
    MetaPrototype objectAttr;

    int sequnece;

    public Long getSuperMetaObjectId() {
        return superMetaObjectId;
    }

    public void setSuperMetaObjectId(Long superMetaObjectId) {
        this.superMetaObjectId = superMetaObjectId;
    }

    public Long getSuperObjectTypeId() {
        return superObjectTypeId;
    }

    public void setSuperObjectTypeId(Long superObjectTypeId) {
        this.superObjectTypeId = superObjectTypeId;
    }

    public MetaPrototype getSuperMetaObject() {
        return superMetaObject;
    }

    public void setSuperMetaObject(MetaPrototype superMetaObject) {
        this.superMetaObject = superMetaObject;
    }

    public Long getSuperObjectAttrId() {
        return superObjectAttrId;
    }

    public void setSuperObjectAttrId(Long superObjectAttrId) {
        this.superObjectAttrId = superObjectAttrId;
    }

    public Long getSuperObjectAttrTypeId() {
        return superObjectAttrTypeId;
    }

    public void setSuperObjectAttrTypeId(Long superObjectAttrTypeId) {
        this.superObjectAttrTypeId = superObjectAttrTypeId;
    }

    public MetaPrototype getSuperObjectAttr() {
        return superObjectAttr;
    }

    public void setSuperObjectAttr(MetaPrototype superObjectAttr) {
        this.superObjectAttr = superObjectAttr;
    }

    public Long getMetaObjectId() {
        return metaObjectId;
    }

    public void setMetaObjectId(Long metaObjectId) {
        this.metaObjectId = metaObjectId;
    }

    public Long getObjectTypeId() {
        return objectTypeId;
    }

    public void setObjectTypeId(Long objectTypeId) {
        this.objectTypeId = objectTypeId;
    }

    public MetaPrototype getMetaObject() {
        return metaObject;
    }

    public void setMetaObject(MetaPrototype metaObject) {
        this.metaObject = metaObject;
    }

    public Long getObjectAttrId() {
        return objectAttrId;
    }

    public void setObjectAttrId(Long objectAttrId) {
        this.objectAttrId = objectAttrId;
    }

    public Long getObjectAttrTypeId() {
        return objectAttrTypeId;
    }

    public void setObjectAttrTypeId(Long objectAttrTypeId) {
        this.objectAttrTypeId = objectAttrTypeId;
    }

    public MetaPrototype getObjectAttr() {
        return objectAttr;
    }

    public void setObjectAttr(MetaPrototype objectAttr) {
        this.objectAttr = objectAttr;
    }

    @Override
    public int getSequnece() {
        return this.sequnece;
    }

    @Override
    public void setSequnece(int i) {
        this.sequnece = i;
    }
}
