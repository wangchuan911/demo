package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;

import java.util.Optional;

/**
 * @Classname ObjectConstructorHandler
 * @Description TODO
 * @Author Septem
 * @Date 23:56
 */
@LinkMetaType.LinkHandle(LinkMetaType.ObjToDataBase)
@Component
public class SqlBuilderHandler implements LinkHandle {


    @Override
    public void handler(HandleContext handleContext, MetaLink metaLink) {
        final SqlContent content = handleContext.get(this, SqlContent::getInstance);
        metaLink.getChildren().forEach(child -> {
            switch (child.getType()) {
                case SqlToSelect: {
                    content.getSelects().add(child);
                    return;
                }
                case SqlToFrom: {
                    content.getJoins().add(child);
                    return;
                }
                default:
                    switch (Optional.ofNullable(child.getType().getParent()).orElse(LinkMetaType.UNKNOWN)) {
                        case SqlToFrom:
                            content.getJoins().add(child);
                            return;
                    }
            }
        });
    }
}
