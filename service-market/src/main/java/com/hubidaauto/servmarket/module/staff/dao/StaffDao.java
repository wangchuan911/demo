package com.hubidaauto.servmarket.module.staff.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname StaffDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 09:46
 */
@Repository
@DS("shop")
public interface StaffDao extends ITemplateDao<Long, StaffVO, StaffCondition> {
}
