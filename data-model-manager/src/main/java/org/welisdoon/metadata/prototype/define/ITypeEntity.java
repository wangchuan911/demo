package org.welisdoon.metadata.prototype.define;

import org.welisdoon.metadata.prototype.consts.IMetaType;

public interface ITypeEntity<T extends IMetaType> {
    T getType();

    default String getTypeDesc() {
        return getType().getDesc();
    }
}
