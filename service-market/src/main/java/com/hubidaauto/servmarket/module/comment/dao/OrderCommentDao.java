package com.hubidaauto.servmarket.module.comment.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.comment.entity.CommentVO;
import com.hubidaauto.servmarket.module.comment.entity.OrderCommentVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname AppEvaluateDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/17 23:02
 */
@Repository
@DS("shop")
public interface OrderCommentDao extends ITemplateDao<Long, OrderCommentVO, OrderCommentVO> {
}
