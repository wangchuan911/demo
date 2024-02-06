package org.welisdoon.metadata.prototype.define;

import org.welisdoon.metadata.prototype.consts.MetaUtils;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaObject extends MetaPrototype {
    Attribute[] attributes;

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * @Classname MetaObject
     * @Description TODO
     * @Author Septem
     * @Date 11:41
     */
    public static class Attribute<T extends MetaObject> extends MetaPrototype {
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
}
