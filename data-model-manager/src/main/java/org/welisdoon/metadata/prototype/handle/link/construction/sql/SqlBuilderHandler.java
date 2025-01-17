package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataBaseTable;
import org.welisdoon.metadata.prototype.entity.DataObject;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.builder.LinkNode;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.builder.SqlContentNode;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.builder.SubSqlContentNode;

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


    @Override
    public void handler(SqlContent content, MetaLink metaLink) {
        MetaObject parent = metaLink.getObject().getParent();
        if (parent instanceof DataObject) {
            SqlContentNode subContent = getSubSqlContent(content, parent.getConstruct(), null);
            subContent.setInstanceId(1L);
            content.addLink(subContent);
        } else if (parent instanceof DataBaseTable) {
            MetaLink mainTable = new MetaLink();
            mainTable.setObjectId(parent.getId());
            mainTable.setObject(parent);
            mainTable.setInstanceId(1L);
            mainTable.setTypeId(LinkMetaType.SqlToJoin.getId());
            mainTable.addChildren(metaLink.getChildren().stream().filter(child -> Objects.equals(child.getType(), LinkMetaType.DataFuture)).toArray(MetaLink[]::new));
            content.addLink(new LinkNode(mainTable, !(content instanceof SqlContentNode)));
        }

        metaLink.getChildren().stream().filter(child -> child.getType().isMatched(LinkMetaType.SqlToJoin, Side.Up)).forEach(child -> {
            if (child.getObjectId() != null && child.getObject().getConstruct() != null) {
                SqlContentNode subContent = getSubSqlContent(content, child.getObject().getConstruct(), child);
                content.addLink(subContent.setInstanceId(child.getInstanceId()));
            } else {
                content.addLink(new LinkNode(child, false));
            }
        });
    }

    protected SqlContentNode getSubSqlContent(SqlContent content, MetaLink subLink, MetaLink parent) {
        SqlContentNode subContent = parent == null ? new SqlContentNode() : new SubSqlContentNode(parent);
        subContent.setUpperContent(content);
        this.handler(subContent, subLink);
        return subContent;
    }
}
