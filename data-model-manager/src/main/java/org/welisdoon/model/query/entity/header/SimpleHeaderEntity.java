package org.welisdoon.model.query.entity.header;

import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.query.entity.AbstractHeaderEntity;

/**
 * @Classname FieldEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:30
 */

public class SimpleHeaderEntity extends AbstractHeaderEntity {
    Long linkId;

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getLinkId() {
        return linkId;
    }
}
