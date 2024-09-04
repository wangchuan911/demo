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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ObjectConstructorHandler
 * @Description TODO
 * @Author Septem
 * @Date 23:56
 */
@LinkMetaType.LinkHandle(LinkMetaType.ObjToDataBase)
@Component
public class SqlBuilderHandler implements LinkHandle {
    List<LinkMetaType> linkMetaTypes = Arrays.asList(LinkMetaType.SqlToJoin, LinkMetaType.SqlToSelect);

    public void setLinkMetaTypes(List<LinkMetaType> linkMetaTypes) {
        this.linkMetaTypes = linkMetaTypes;
    }

    @Override
    public void handler(HandleContext handleContext, MetaLink metaLink) {
        MetaObject parent = metaLink.getObject().getParent();
        SqlContent content;
        if (parent instanceof DataObject) {
            MetaLink parentLink = new MetaLink();
//            parentLink.setId(Long.MIN_VALUE);
            parentLink.setObjectId(parent.getId());
            parentLink.setObject(parent);
            this.handler(handleContext, parentLink);
            content = handleContext.set(this, () -> {
                SqlContent parentContent = handleContext.get(SqlBuilderHandler.this);
                SqlContent content1 = SqlContent.getInstance();
                content1.setParent(parentContent);
                return content1;
            });
        } else if (parent instanceof DataBaseTable) {
            MetaLink mainTable = new MetaLink();
//            mainTable.setId(Long.MIN_VALUE);
            mainTable.setObjectId(parent.getId());
            mainTable.setObject(parent);
            mainTable.setInstanceId(1L);
            mainTable.setTypeId(LinkMetaType.SqlToJoin.getId());
            mainTable.addChildren(metaLink.getChildren().stream().filter(child -> Objects.equals(child.getType(), LinkMetaType.DataFuture)).toArray(MetaLink[]::new));
            content = handleContext.get(this, SqlContent::getInstance);
            content.addLink(mainTable);
        } else {
            content = handleContext.get(this, SqlContent::getInstance);
        }

        metaLink.getChildren().forEach(child -> {
            for (LinkMetaType type : linkMetaTypes) {
                if (child.getType().isMatched(type, Side.Up)) {
                    content.addLink(child);
                    return;
                }
            }
        });
    }
}
