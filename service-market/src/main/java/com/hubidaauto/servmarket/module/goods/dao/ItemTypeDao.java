package com.hubidaauto.servmarket.module.goods.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
@DS("shop")
public interface ItemTypeDao extends ITemplateDao<Long, ItemTypeVO, ItemTypeCondition> {
}
