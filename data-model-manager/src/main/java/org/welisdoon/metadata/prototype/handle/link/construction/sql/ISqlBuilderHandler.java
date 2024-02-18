package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname ISqlBuilderHandler
 * @Description TODO
 * @Author Septem
 * @Date 16:48
 */
public interface ISqlBuilderHandler {
    Map<LinkMetaType, ISqlBuilderHandler> handlerMap = new HashMap<>();

    String toSql(MetaLink parentML, SqlContent sqlContent);

    default String toChildSql(MetaLink child, SqlContent sqlContent) {
        ISqlBuilderHandler iSqlBuilderHandler = getHandler(child.getType());
        Assert.notNull(iSqlBuilderHandler, "未定义iSqlBuilderHandler的linkType:" + child.getType());
        return iSqlBuilderHandler.toSql(child, sqlContent);
    }

    static ISqlBuilderHandler getHandler(LinkMetaType type) {
        ObjectUtils.synchronizedInitial(handlerMap, handlerMap -> handlerMap.size() > 0, handlerMap -> {
            ApplicationContextProvider.getApplicationContext().getBeansOfType(ISqlBuilderHandler.class).entrySet()
                    .stream().forEach(entry -> {
                LinkMetaType.LinkHandle linkHandle = ApplicationContextProvider.getRealClass(entry.getValue().getClass()).getAnnotation(LinkMetaType.LinkHandle.class);
                if (Objects.isNull(linkHandle)) return;
                for (LinkMetaType linkMetaType : linkHandle.value()) {
                    handlerMap.put(linkMetaType, entry.getValue());
                }
            });
        });
        return handlerMap.get(type);
    }

    default String linkToSql(MetaLink metaLink) {
        if (Objects.isNull(metaLink))
            return "null";

        return String.format("T%s.%s", metaLink.getInstanceId(), metaLink.getCode());
    }

    default String operator(LinkMetaType type) {
        return type.name();
    }

}
