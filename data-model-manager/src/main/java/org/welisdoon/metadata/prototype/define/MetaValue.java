package org.welisdoon.metadata.prototype.define;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.KeyValueMetaType;
import org.welisdoon.metadata.prototype.consts.KeyValueType;
import org.welisdoon.metadata.prototype.dao.MetaValueDao;
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
public class MetaValue extends MetaPrototype<MetaValue> implements ISequenceEntity {
    final int LENGTH = 4000;
    String value;
    Long valueTypeId;
    KeyValueMetaType type;
    KeyValueType valueType;
    int sequence;
    boolean bigFile;

    public String getValue() {
        if (bigFile) {
            return getChildren().stream().filter(metaValue -> !Objects.equals(metaValue.getValueType(), KeyValueType.UNKNOWN)).map(MetaValue::getValue).collect(Collectors.joining());
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
            Assert.notNull(getValueTypeId(), "请先初始化值类型");
            KeyValueType valueType = getValueType();
            valueType = KeyValueType.Bigfile;
            int size = length / 4000;
            byte[] bytes = value.getBytes();
            for (int i = 0; i < size; i++) {
                if (i >= getChildren().size()) {
                    MetaValue keyValue = new MetaValue();
                    keyValue.setParentId(this.getId());
                    keyValue.setSequence(i);
                    keyValue.setValueTypeId(valueType.getId());
                    getChildren().add(keyValue);
                }
                getChildren().get(i).setValueTypeId(valueType.getId());
                getChildren().get(i).setValue(new String(ArrayUtils.subarray(bytes, i * LENGTH, (1 + i) * LENGTH)));
            }
            if (getChildren().size() > size) {
                getChildren().stream().skip(size).forEach(metaValue -> {
                    metaValue.setValueTypeId(KeyValueMetaType.UNKNOWN.getId());
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
        try {
            this.valueTypeId = valueTypeId;
        } finally {
            Optional.ofNullable(valueType).orElseGet(() -> {
                valueType = KeyValueType.getInstance(valueTypeId);
                return valueType;
            });
            bigFile = valueType == KeyValueType.Bigfile;
        }
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
    public MetaValue setTypeId(Long typeId) {
        return super.setTypeId(typeId);
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

    public String toSql() {
        switch (getValueType()) {
            case String:
            case Char:
                return String.format("'%s'", value);
            case Numeric:
                return value;
            default:
                throw new IllegalStateException(String.format("错误的类型%s", valueType));
        }
    }

    @Override
    public List<MetaValue> getChildren() {
        ObjectUtils.synchronizedInitial(this, metaValue -> Objects.nonNull(children), metaValue -> {
            children = ApplicationContextProvider.getBean(MetaValueDao.class).list(new MetaValue().setParentId(this.getId()));
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
