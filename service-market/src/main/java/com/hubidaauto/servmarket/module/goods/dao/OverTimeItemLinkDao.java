package com.hubidaauto.servmarket.module.goods.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.goods.entity.OverTimeItemVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname OverTimeItemLink
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/4 12:32
 */
@Repository
@DS("shop")
public interface OverTimeItemLinkDao extends ITemplateDao<Long, OverTimeItemVO, OverTimeItemVO> {

}
