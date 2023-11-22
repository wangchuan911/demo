package org.welisdoon.metadata.prototype.handle.link.construction;

import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.HandleParameter;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;

/**
 * @Classname ObjectConstructorHandler
 * @Description TODO
 * @Author Septem
 * @Date 23:56
 */
@LinkMetaType.LinkHandle({LinkMetaType.ObjToConstruction})
public class ObjectConstructorHandler implements LinkHandle {
    @Autowired
    MetaLinkDao metaObjectDao;

    @Override
    public void handler(HandleParameter handleParameter, MetaLink... metaLinks) {
        handleParameter.setCurrentInstance(new ObjectConstructorParams());
        handlerToLowerLink(metaLinks[0], handleParameter);
    }

    public class ObjectConstructorParams {
    }
}
