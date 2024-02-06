package org.welisdoon.metadata.prototype.handle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.util.Objects;

/**
 * @Classname MetaObjectHandler
 * @Description TODO
 * @Author Septem
 * @Date 16:55
 */
@Component
public class MetaObjectUtils {

    static MetaObjectDao metaObjectDao;

    @Autowired
    public void setMetaObjectDao(MetaObjectDao metaObjectDao) {
        this.metaObjectDao = metaObjectDao;
    }

    public static MetaObject getParent(MetaObject metaObject) {
        if (Objects.isNull(metaObject.getParentId()))
            return null;
        if (Objects.isNull(metaObject.getParent()))
            metaObject.setParent(metaObjectDao.get(metaObject.getParentId()));

        return metaObject.getParent();
    }

    public static MetaObject getParent(MetaObject metaObject, ObjectMetaType metaType) {
        MetaObject parent = getParent(metaObject);
        while (parent != null && !Objects.equals(parent.getTypeId(), metaType.getId()))
            parent = getParent(parent);

        return parent;
    }
}
