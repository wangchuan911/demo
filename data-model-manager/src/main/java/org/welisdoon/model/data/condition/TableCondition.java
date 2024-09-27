package org.welisdoon.model.data.condition;

import com.alibaba.fastjson.annotation.JSONField;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.model.data.entity.database.TableEntity;

public class TableCondition extends BaseCondition<Long, TableEntity> {
    @Override
    @JSONField(deserializeUsing = org.welisdoon.metadata.prototype.condition.Page.class)
    public Page getPage() {
        return super.getPage();
    }
}
