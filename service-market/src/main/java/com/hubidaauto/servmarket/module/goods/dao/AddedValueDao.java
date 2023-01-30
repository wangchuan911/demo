package com.hubidaauto.servmarket.module.goods.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.goods.entity.AddedValueVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname AddedValueDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/10/2 23:46
 */
@Repository
@DS("shop")
public interface AddedValueDao extends ITemplateDao<Long, AddedValueVO, AddedValueVO> {
}
