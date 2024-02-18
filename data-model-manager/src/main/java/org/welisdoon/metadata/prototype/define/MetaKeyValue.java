package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaKeyValue
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
public class MetaKeyValue extends MetaPrototype<MetaKeyValue> implements ISequenceEntity {
    String value;
    Long valueTypeId;

    int sequence;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getValueTypeId() {
        return valueTypeId;
    }

    public void setValueTypeId(Long valueTypeId) {
        this.valueTypeId = valueTypeId;
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
