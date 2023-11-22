package org.welisdoon.metadata.prototype.handle.link;

import org.apache.commons.lang.ArrayUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
import org.welisdoon.metadata.prototype.handle.HandleParameter;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @Classname LinkHandle
 * @Description TODO
 * @Author Septem
 * @Date 18:14
 */
public interface LinkHandle {

    void handler(HandleParameter handleParameter, MetaLink... metaLinks);

    default void handlerToLowerLink(MetaLink metaLink, HandleParameter handleParameter) {
        List<MetaLink> list = ApplicationContextProvider.getBean(MetaLinkDao.class).list(new MetaLinkCondition().setParentId(metaLink.getId()));
        long[] typeId = list.stream().map(MetaPrototype::getTypeId).mapToLong(Long::longValue).toArray();
        Optional optional = ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(LinkMetaType.LinkHandle.class)
                .values().stream().filter(o -> {
                    if (!(o instanceof LinkHandle)) {
                        return false;
                    }
                    long[] linkMetaTypes = Arrays.stream(ApplicationContextProvider.getRealClass(o.getClass()).getAnnotation(LinkMetaType.LinkHandle.class).value()).mapToLong(value -> value.getId()).toArray();
                    return Arrays.stream(linkMetaTypes).filter(metaType -> !ArrayUtils.contains(typeId, metaType)).count() == 0;
                })
                .findFirst();
        if (optional.isEmpty()) {
            return;
        }
        ((LinkHandle) optional.get()).handler(handleParameter, list.toArray(MetaLink[]::new));
    }

}
