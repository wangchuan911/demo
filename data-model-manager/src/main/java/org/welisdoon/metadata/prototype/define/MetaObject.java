package org.welisdoon.metadata.prototype.define;

import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Optional;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaObject extends MetaPrototype<MetaObject> {
    Attribute[] attributes;

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public Attribute[] getAttributes() {
        return Optional.ofNullable(attributes).orElseGet(() ->
                attributes = ApplicationContextProvider.getBean(MetaAttributeDao.class).list(new Attribute().setObjectId(this.getId())).stream().toArray(Attribute[]::new)
        );
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

        public Attribute setObjectId(Long objectId) {
            this.objectId = objectId;
            return this;
        }

        public T getObject() {
            return (T) MetaUtils.getInstance().getObject(id);
        }
    }
}
