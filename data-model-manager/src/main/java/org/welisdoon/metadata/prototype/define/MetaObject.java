package org.welisdoon.metadata.prototype.define;

import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Objects;
import java.util.Optional;

/**
 * @Classname MetaObject
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
public class MetaObject extends MetaPrototype<MetaObject> {
    Attribute[] attributes;
    ObjectMetaType type;

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public Attribute[] attributes() {
        return Optional.ofNullable(attributes).orElseGet(() ->
                attributes = ApplicationContextProvider.getBean(MetaAttributeDao.class).list(new Attribute().setObjectId(this.getId())).stream().toArray(Attribute[]::new)
        );
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public ObjectMetaType type() {
        return Optional.ofNullable(type).orElseGet(() -> {
            type = ObjectMetaType.getInstance(typeId);
            return type;
        });
    }

    public ObjectMetaType getType() {
        return type;
    }

    @Override
    public MetaObject parent() {
        if (Objects.isNull(parentId))
            return null;
        ObjectUtils.synchronizedInitial(this,
                tAttribute -> Objects.nonNull(parent),
                tAttribute ->
                        parent = (MetaObject) ApplicationContextProvider.getBean(MetaUtils.class).getType(ApplicationContextProvider.getBean(MetaObjectDao.class).get(parentId))
        );
        return super.parent();
    }

    /**
     * @Classname MetaObject
     * @Description TODO
     * @Author Septem
     * @Date 11:41
     */
    public static class Attribute<T extends MetaObject> extends MetaPrototype<Attribute> {
        Long objectId;
        AttributeMetaType type;

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

        public AttributeMetaType type() {
            return Optional.ofNullable(type).orElseGet(() -> {
                type = AttributeMetaType.getInstance(typeId);
                return type;
            });
        }

        public AttributeMetaType getType() {
            return type;
        }

        @Override
        public Attribute parent() {
            if (Objects.isNull(parentId))
                return null;
            ObjectUtils.synchronizedInitial(this,
                    tAttribute -> Objects.nonNull(parent),
                    tAttribute -> parent = ApplicationContextProvider.getBean(MetaAttributeDao.class).get(parentId));
            return super.parent();
        }

        public Attribute parent(AttributeMetaType type) {
            Attribute attribute = parent();
            while (Objects.nonNull(attribute) && attribute.type() != type) {
                attribute = attribute.parent();
            }
            return attribute;
        }
    }
}
