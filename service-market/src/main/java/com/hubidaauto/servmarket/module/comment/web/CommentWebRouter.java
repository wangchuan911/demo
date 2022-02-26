package com.hubidaauto.servmarket.module.comment.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.comment.dao.OrderCommentDao;
import com.hubidaauto.servmarket.module.comment.entity.OrderCommentCondition;
import com.hubidaauto.servmarket.module.comment.entity.OrderCommentVO;
import com.hubidaauto.servmarket.module.common.annotation.ContentType;
import com.hubidaauto.servmarket.module.common.dao.ImageContentDao;
import com.hubidaauto.servmarket.module.common.entity.ImageContentVO;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.List;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath(prefix = "{wechat-app-hubida.path.app}/comment")
public class CommentWebRouter {
    OrderCommentDao orderCommentDao;

    ImageContentDao imageContentDao;

    @Autowired
    public void setOrderCommentDao(OrderCommentDao orderCommentDao) {
        this.orderCommentDao = orderCommentDao;
    }

    @Autowired
    public void setImageContentDao(ImageContentDao imageContentDao) {
        this.imageContentDao = imageContentDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }

    @VertxRouter(path = "/order",
            method = "POST")
    public void add(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            JSONObject jsonObject = JSONObject.parseObject(routingContext.getBodyAsString());
            OrderCommentVO evaluateVO = jsonObject.toJavaObject(OrderCommentVO.class);
            if (evaluateVO == null || orderCommentDao.add(evaluateVO) == 0) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            ContentType type = evaluateVO.getClass().getAnnotation(ContentType.class);
            if (jsonObject.containsKey("imgIds")) {
                List<Long> imgIds = jsonObject.getJSONArray("imgIds").toJavaList(Long.class);
                ImageContentVO imageContent;
                for (Long imgId : imgIds) {
                    imageContent = imageContentDao.get(imgId);
                    imageContent.setRefId(evaluateVO.getId());
                    imageContent.setType(type.id());
                    imageContentDao.put(imageContent);
                }
            }

            routingContext.end("成功");
        });
    }

    @VertxRouter(path = "\\/orders\\/(?<page>\\d+)",
            mode = VertxRouteType.PathRegex,
            method = "POST")
    public void list(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {

            try {
                OrderCommentCondition evaluateVO = JSONObject.parseObject(routingContext.getBodyAsString(), OrderCommentCondition.class);
                evaluateVO.page(Integer.parseInt(routingContext.pathParam("page")));
                routingContext.end(JSON.toJSONString(orderCommentDao.list(evaluateVO)));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }


}
