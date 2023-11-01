package org.welisdoon.metadata.prototype.entity;

import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.define.MetaAttribute;
import org.welisdoon.metadata.prototype.define.MetaObject;

/**
 * @Classname DataObject
 * @Description TODO
 * @Author Septem
 * @Date 14:49
 */
@ObjectMetaType.MetaType(ObjectMetaType.Object)
public class DataObject extends MetaObject {
    Attributes[] attributes;

    public DataObject(long id) {

    }

    public Attributes[] getAttributes() {
        return attributes;
    }

    @AttributeMetaType.MetaType(AttributeMetaType.Attributes)
    public static class Attributes extends MetaAttribute {

    }
}
