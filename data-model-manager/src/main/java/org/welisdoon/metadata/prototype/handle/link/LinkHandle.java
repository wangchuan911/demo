package org.welisdoon.metadata.prototype.handle.link;

import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Objects;

/**
 * @Classname LinkHandle
 * @Description TODO
 * @Author Septem
 * @Date 18:14
 */
@FunctionalInterface
public interface LinkHandle {

    void handler(HandleContext handleContext, MetaLink metaLink);

    default void execute(HandleContext handleContext, MetaLink metaLink) {
        execute(handleContext, metaLink, this);
    }

    default void execute(HandleContext handleContext, MetaLink metaLink, LinkHandle linkHandle) {
        MetaLinkDao metaObjectDao = ApplicationContextProvider.getBean(MetaLinkDao.class);
        if (Objects.isNull(metaLink) || Objects.isNull(metaLink.getId())) {
            return;
        }
        for (MetaLink link : metaObjectDao.list(new MetaLinkCondition().setParentId(metaLink.getId()))) {
            link.setParent(link);
            linkHandle.handler(handleContext, metaLink);
        }
    }
}
