package org.welisdoon.model.query.entity.query;

import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.query.entity.AbstractQueryEntity;

/**
 * @Classname HeaderLinkEntity
 * @Description TODO
 * @Author Septem
 * @Date 13:41
 */
public class HeaderLinkEntity extends AbstractQueryEntity<DataObjectEntity> {
    Long targetId, targetTypeId;
    HeaderLinkEntity[] children;

    public void setTargetTypeId(Long targetTypeId) {
        this.targetTypeId = targetTypeId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public Long getTargetTypeId() {
        return targetTypeId;
    }

    public HeaderLinkEntity[] getChildren() {
        return children;
    }

    public void setChildren(HeaderLinkEntity[] children) {
        this.children = children;
    }
}
