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
            return children().stream().filter(metaValue -> !Objects.equals(metaValue.valueType(), KeyValueType.UNKNOWN)).map(MetaValue::getValue).collect(Collectors.joining());
        }
        return value;
    }

    public void setValue(String value) {
        int length = value.getBytes().length;
        if (bigFile && length <= 4000) {
            valueType = valueType();
            bigFile = false;
            this.value = value;
            return;
        }
        if (length > 4000) {
            bigFile = true;
            Assert.notNull(getValueTypeId(), "请先初始化值类型");
            KeyValueType valueType = valueType();
            valueType = KeyValueType.Bigfile;
            int size = length / 4000;
            byte[] bytes = value.getBytes();
            for (int i = 0; i < size; i++) {
                if (i >= children().size()) {
                    MetaValue keyValue = new MetaValue();
                    keyValue.setParentId(this.getId());
                    keyValue.setSequence(i);
                    keyValue.setValueTypeId(valueType.getId());
                    children().add(keyValue);
                }
                children().get(i).setValueTypeId(valueType.getId());
                children().get(i).setValue(new String(ArrayUtils.subarray(bytes, i * LENGTH, (1 + i) * LENGTH)));
            }
            if (children().size() > size) {
                children().stream().skip(size).forEach(metaValue -> {
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

    public KeyValueMetaType type() {
        Optional.ofNullable(type).orElseGet(() -> {
            type = KeyValueMetaType.getInstance(typeId);
            return type;
        });
        return type;
    }

    public KeyValueMetaType getType() {
        return type;
    }

    public KeyValueType valueType() {
        if (valueType == KeyValueType.Bigfile) {
            return CollectionUtils.isEmpty(children()) ? KeyValueType.UNKNOWN : children().get(0).valueType;
        }
        return valueType;
    }

    public KeyValueType getValueType() {
        return valueType;
    }


    public String toSql() {
        switch (valueType()) {
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
    public List<MetaValue> children() {
        ObjectUtils.synchronizedInitial(this, metaValue -> Objects.nonNull(children), metaValue -> {
            children = ApplicationContextProvider.getBean(MetaValueDao.class).list(new MetaValue().setParentId(this.getId()));
        });
        return super.children();
    }

    public void setBigFile(boolean bigFile) {
        this.bigFile = bigFile;
    }

    public boolean isBigFile() {
        return bigFile;
    }
}
