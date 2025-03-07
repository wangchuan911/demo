package org.welisdoon.metadata.prototype.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.KeyValueMetaType;
import org.welisdoon.metadata.prototype.consts.KeyValueType;
import org.welisdoon.metadata.prototype.dao.MetaValueDao;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Classname MetaKeyValue
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
public class MetaValue extends MetaPrototype implements ISequenceEntity, ITypeEntity<KeyValueMetaType>, MetaPrototype.Child<MetaValue>, MetaPrototype.Parent<MetaValue> {
    final int LENGTH = 4000;
    String value;
    Long valueTypeId;
    KeyValueMetaType type;
    KeyValueType valueType;
    int sequence;
    boolean bigFile;
    MetaProtoList<MetaValue> children = new MetaProtoList<>();
    MetaValue parent;

    public String getValue() {
        if (bigFile) {
            return getChildren().stream().filter(metaValue -> !Objects.equals(metaValue.valueType(), KeyValueType.UNKNOWN)).map(MetaValue::getValue).collect(Collectors.joining());
        }
        return value;
    }

    public void setValue(String value) {
        setEditing(getValue(), value);
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
        setEditing(this.valueTypeId, valueTypeId);
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
    public MetaValue setSequence(int sequence) {
        setEditing(this.sequence, sequence);
        this.sequence = sequence;
        return this;
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

    public KeyValueType valueType() {
        if (valueType == KeyValueType.Bigfile) {
            return CollectionUtils.isEmpty(getChildren()) ? KeyValueType.UNKNOWN : getChildren().get(0).valueType;
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
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public MetaProtoList<MetaValue> getChildren() {
        ObjectUtils.synchronizedInitial(children, metaValue -> children.getState() != MetaProtoList.LifeState.Initial, metaValue -> {
            setChildren(ApplicationContextProvider.getBean(MetaValueDao.class).list(new MetaValue().setParentId(this.getId())));
            children.setState(MetaProtoList.LifeState.Loaded);
        });
        return children;
    }

    public void setBigFile(boolean bigFile) {
        setEditing(this.bigFile, bigFile);
        this.bigFile = bigFile;
    }

    public boolean isBigFile() {
        return bigFile;
    }

    @Override
    public MetaValue getParent() {
        return parent;
    }

    @Override
    public MetaValue setParent(MetaValue parent) {
        this.parent = parent;
        this.parentId = this.parent == null ? null : this.parent.getId();
        return this;
    }

    @Override
    public int remove() {
        if (getState() == LifeState.Delete)
            return 0;
        super.remove();
        setState(LifeState.Delete);
        return 0;
    }

    @Override
    public int save() {
        super.save();
        return 0;
    }

}
