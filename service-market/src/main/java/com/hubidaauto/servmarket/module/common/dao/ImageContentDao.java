package com.hubidaauto.servmarket.module.common.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.entity.ImageContentVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
@DS("shop")
public interface ImageContentDao extends ITemplateDao<Long, ImageContentVO, ImageContentVO> {

}
