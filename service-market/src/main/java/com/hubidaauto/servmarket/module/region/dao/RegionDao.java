package com.hubidaauto.servmarket.module.region.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.region.entity.RegionCondition;
import com.hubidaauto.servmarket.module.region.entity.RegionVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname RegionDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/11 13:32
 */
@Repository
@DS("shop")
public interface RegionDao extends ITemplateDao<Long, RegionVO, RegionCondition> {
}
