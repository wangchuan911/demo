package org.welisdoon.metadata.prototype.define;

import org.welisdoon.metadata.prototype.consts.MetaUtils;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaAttribute<T extends MetaObject> extends MetaPrototype {
    Long objectId;

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public T getObject() {
        return (T) MetaUtils.getObject(id);
    }
}
