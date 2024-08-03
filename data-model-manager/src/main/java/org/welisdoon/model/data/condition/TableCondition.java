package org.welisdoon.model.data.condition;

import com.github.pagehelper.PageHelper;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.model.data.entity.database.TableEntity;

public class TableCondition extends BaseCondition<Long, TableEntity> {
    @Override
    public <T extends BaseCondition> T startPage() {
        if (getPage() != null)
            PageHelper.startPage(this.getPage().getPage(), this.getPage().getPageSize());
        return super.startPage();
    }
}
