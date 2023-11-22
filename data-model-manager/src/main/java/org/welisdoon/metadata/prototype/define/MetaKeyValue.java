package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaKeyValue
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
public class MetaKeyValue extends MetaPrototype implements ISequenceEntity {
    Long targetObjectId, targetObjectAttrId;

    int sequence;

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
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
