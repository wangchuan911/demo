package com.hubidaauto.servmarket.module.message.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.message.entity.MesseageTemplate;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname MessageTemplateDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/17 21:25
 */
@Repository
@DS("shop")
public interface MesseageTemplateDao extends ITemplateDao<Long, MesseageTemplate, MesseageTemplate> {
}
