package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaKeyValue
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
public class MetaKeyValue extends MetaPrototype implements ISequneceEntity {
    Long targetObjectId, targetObjectAttrId;

    int sequnece;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(Long targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public Long getTargetObjectAttrId() {
        return targetObjectAttrId;
    }

    public void setTargetObjectAttrId(Long targetObjectAttrId) {
        this.targetObjectAttrId = targetObjectAttrId;
    }

    @Override
    public int getSequnece() {
        return sequnece;
    }

    @Override
    public void setSequnece(int sequnece) {
        this.sequnece = sequnece;
    }
}
