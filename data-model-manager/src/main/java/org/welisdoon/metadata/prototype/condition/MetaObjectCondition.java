package org.welisdoon.metadata.prototype.condition;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(deserializeUsing = org.welisdoon.metadata.prototype.condition.Page.class)
    public void setPage(Page page) {
        super.setPage(page);
    }
}
