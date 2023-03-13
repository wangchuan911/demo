package org.welisdoon.model.data.entity.common;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.annotations.Common;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.CommonType;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;

/**
 * @Classname DictEntity
 * @Description TODO
 * @Author Septem
 * @Date 9:54
 */
public abstract class AbstractCommonEntity<DATA_MARKER extends Enum<? extends IData.IDataMarker>> extends IData.DataObject<Long, DATA_MARKER> {
    String code, name;

    public String getCode() {
        return code;
    }

    public <T extends AbstractCommonEntity> T setCode(String code) {
        this.code = code;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public <T extends AbstractCommonEntity> T setName(String name) {
        this.name = name;
        return (T) this;
    }
}
