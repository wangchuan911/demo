package org.welisdoon.metadata.prototype.condition;

import com.github.pagehelper.PageHelper;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.metadata.prototype.define.MetaObject;

/**
 * @Classname MetaObjectCondition
 * @Description TODO
 * @Author Septem
 * @Date 18:04
 */
public class MetaObjectCondition extends BaseCondition<Long, MetaObject> {
    Long parentId;

    public MetaObjectCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public <T extends BaseCondition> T startPage() {
        if (getPage() != null)
            PageHelper.startPage(this.getPage().getPage(), this.getPage().getPageSize());
        return super.startPage();
    }
}
