package org.welisdoon.model.query.entity.header;

import org.welisdoon.model.query.entity.AbstractHeaderEntity;

/**
 * @Classname FieldEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:30
 */

public class GroupHeaderEntity extends SimpleHeaderEntity {
    AbstractHeaderEntity[] children;

    public void setChildren(AbstractHeaderEntity[] children) {
        this.children = children;
    }

    public AbstractHeaderEntity[] getChildren() {
        return children;
    }
}
