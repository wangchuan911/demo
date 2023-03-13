package org.welisdoon.model.data.entity.common;

import org.welisdoon.model.data.annotations.Common;
import org.welisdoon.model.data.consts.CommonType;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;

/**
 * @Classname DictEntity
 * @Description TODO
 * @Author Septem
 * @Date 9:54
 */
@Common(CommonType.Dict)
public class DictEntity extends AbstractCommonEntity {
    String group, value;

    public String getGroup() {
        return group;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
