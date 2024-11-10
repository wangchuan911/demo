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
public class SqlBuilderHandler implements LinkHandle<SqlContent> {
    List<LinkMetaType> linkMetaTypes = Arrays.asList(LinkMetaType.SqlToJoin, LinkMetaType.SqlToSelect);

    public void setLinkMetaTypes(List<LinkMetaType> linkMetaTypes) {
        this.linkMetaTypes = linkMetaTypes;
    }

    @Override
    public void handler(SqlContent content, MetaLink metaLink) {
        MetaObject parent = metaLink.getObject().getParent();
        if (parent instanceof DataObject) {
            MetaLink parentLink = new MetaLink();
            parentLink.setObjectId(parent.getId());
            parentLink.setObject(parent);
            SqlContent parentObjectContent = SqlContent.getInstance();
            this.handler(parentObjectContent, parentLink);
            content.setParent(parentObjectContent);
        } else if (parent instanceof DataBaseTable) {
            MetaLink mainTable = new MetaLink();
            mainTable.setObjectId(parent.getId());
            mainTable.setObject(parent);
            mainTable.setInstanceId(1L);
            mainTable.setTypeId(LinkMetaType.SqlToJoin.getId());
            mainTable.addChildren(metaLink.getChildren().stream().filter(child -> Objects.equals(child.getType(), LinkMetaType.DataFuture)).toArray(MetaLink[]::new));
            content.addLink(mainTable);
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
