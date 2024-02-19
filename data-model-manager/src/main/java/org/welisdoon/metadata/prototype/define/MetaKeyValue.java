package org.welisdoon.metadata.prototype.define;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.KeyValueMetaType;
import org.welisdoon.metadata.prototype.consts.KeyValueType;
import org.welisdoon.metadata.prototype.dao.MetaKeyValueDao;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Classname MetaKeyValue
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
public class MetaKeyValue extends MetaPrototype<MetaKeyValue> implements ISequenceEntity {
    final int LENGTH = 4000;
    String value;
    Long valueTypeId;
    KeyValueMetaType type;
    KeyValueType valueType;
    int sequence;
    boolean bigFile;

    public String getValue() {
        if (bigFile) {
            return getChildren().stream().filter(metaKeyValue -> !Objects.equals(metaKeyValue.getValueType(), KeyValueType.UNKNOWN)).map(MetaKeyValue::getValue).collect(Collectors.joining());
        }
        return value;
    }

    public void setValue(String value) {
        int length = value.getBytes().length;
        if (bigFile && length <= 4000) {
            valueType = getValueType();
            bigFile = false;
            this.value = value;
            return;
        }
        if (length > 4000) {
            bigFile = true;
            valueType = getValueType();
            int size = length / 4000;
            byte[] bytes = value.getBytes();
            for (int i = 0; i < size; i++) {
                if (i > getChildren().size()) {
                    MetaKeyValue keyValue = new MetaKeyValue();
                    keyValue.setParentId(this.getId());
                    keyValue.setSequence(i);
                    keyValue.setValueTypeId(valueType.getId());
                    getChildren().add(keyValue);
                }
                if (i < getChildren().size()) {
                    getChildren().get(i).setValueTypeId(valueType.getId());
                    getChildren().get(i).setValue(new String(ArrayUtils.subarray(bytes, i * LENGTH, (1 + i) * LENGTH)));
                }
            }
            if (getChildren().size() > size) {
                getChildren().stream().skip(size).forEach(metaKeyValue -> {
                    metaKeyValue.setValueTypeId(KeyValueMetaType.UNKNOWN.getId());
                });
            }
            return;
        }
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

    @Override
    public MetaKeyValue setTypeId(Long typeId) {
        try {
            return super.setTypeId(typeId);
        } finally {
            Optional.ofNullable(valueType).orElseGet(() -> {
                valueType = KeyValueType.getInstance(valueTypeId);
                return valueType;
            });
            bigFile = valueType == KeyValueType.Bigfile;
        }
    }

    public KeyValueMetaType getType() {
        Optional.ofNullable(type).orElseGet(() -> {
            type = KeyValueMetaType.getInstance(typeId);
            return type;
        });
        return type;
    }


    public KeyValueType getValueType() {
        if (valueType == KeyValueType.Bigfile) {
            return CollectionUtils.isEmpty(getChildren()) ? KeyValueType.UNKNOWN : getChildren().get(0).valueType;
        }
        return valueType;
    }

    @Override
    public List<MetaKeyValue> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaKeyValue -> Objects.nonNull(children), metaKeyValue -> {
            children = ApplicationContextProvider.getBean(MetaKeyValueDao.class).list(new MetaKeyValue().setParentId(this.getId()));
        });
        return super.getChildren();
    }

    public void setBigFile(boolean bigFile) {
        this.bigFile = bigFile;
    }

    public boolean isBigFile() {
        return bigFile;
    }
}
