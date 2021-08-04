package com.hubidaauto.servmarket.module.goods.dao;

import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;
@Repository
public interface ItemDao extends ITemplateDao<Long, ItemVO, ItemCondition> {
}
