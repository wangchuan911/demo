package com.hubidaauto.servmarket.module.common.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.entity.AppConfig;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname AppConfigDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/10/12 22:07
 */
@Repository
@DS("shop")
public interface AppConfigDao extends ITemplateDao<String, AppConfig, AppConfig> {
}
