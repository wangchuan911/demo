package com.hubidaauto.servmarket.module.workorder.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
@DS("shop")
public interface ServiceClassWorkOrderDao extends ITemplateDao<Long, ServiceClassWorkOrderVO, ServiceClassWorkOrderCondition> {
}
