package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaObject extends MetaPrototype {
    MetaObjectAttribute[] attributes;

    public void setAttributes(MetaObjectAttribute[] attributes) {
        this.attributes = attributes;
    }

    public MetaObjectAttribute[] getAttributes() {
        return attributes;
    }
}
