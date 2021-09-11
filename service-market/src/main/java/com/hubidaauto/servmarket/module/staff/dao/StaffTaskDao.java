package com.hubidaauto.servmarket.module.staff.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffTaskVO;
import com.hubidaauto.servmarket.module.staff.entity.StaffVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname StaffTask
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/11 15:06
 */
@Repository
@DS("shop")
public interface StaffTaskDao extends ITemplateDao<Long[], StaffTaskVO, StaffCondition> {
}
