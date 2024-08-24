package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
import org.welisdoon.metadata.prototype.entity.DataObject;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;

import java.util.Objects;
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
        MetaObject parent = metaLink.getObject().getParent();
        if (parent instanceof DataObject) {
            MetaLink parentLink = new MetaLink();
//            parentLink.setId(Long.MIN_VALUE);
            parentLink.setObjectId(parent.getId());
            parentLink.setObject(parent);
            this.handler(handleContext, parentLink);
        } else if (parent instanceof DataBaseTable) {
            MetaLink mainTable = new MetaLink();
//            mainTable.setId(Long.MIN_VALUE);
            mainTable.setObjectId(parent.getId());
            mainTable.setObject(parent);
            mainTable.setInstanceId(1L);
            mainTable.setTypeId(LinkMetaType.SqlToJoin.getId());
            mainTable.addChildren(metaLink.getChildren().stream().filter(child -> Objects.equals(child.getType(), LinkMetaType.DataFuture)).toArray(MetaLink[]::new));
            content.getJoins().add(mainTable);
        }

        metaLink.getChildren().forEach(child -> {
            switch (child.getType()) {
                case SqlToSelect: {
                    content.getSelects().add(child);
                    return;
                }
                case SqlToJoin: {
                    content.getJoins().add(child);
                    return;
                }
                default:
                    if (child.getType().isMatched(LinkMetaType.SqlToJoin, Side.Up)) {
                        content.getJoins().add(child);
                        return;
                    }
            }
        });
    }
}
