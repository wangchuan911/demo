package org.welisdoon.model.data.dao;

import com.github.pagehelper.Page;
import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.TableCondition;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
public interface TableDao extends ITemplateDao<Long, TableEntity, TableCondition> {

}
