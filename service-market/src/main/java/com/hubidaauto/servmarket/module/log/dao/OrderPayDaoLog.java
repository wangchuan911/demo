package com.hubidaauto.servmarket.module.log.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.log.entity.OrderPayLogVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname PrePayDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/11 17:32
 */
@Repository
@DS("shop")
public interface OrderPayDaoLog extends ITemplateDao<Long, OrderPayLogVO, OrderPayLogVO> {
}
