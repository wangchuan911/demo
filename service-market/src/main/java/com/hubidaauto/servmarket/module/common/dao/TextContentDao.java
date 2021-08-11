package com.hubidaauto.servmarket.module.common.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.entity.ContentVO;
import com.hubidaauto.servmarket.module.common.entity.TextContentVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
@DS("shop")
public interface TextContentDao extends ITemplateDao<Long, TextContentVO, TextContentVO> {

}
