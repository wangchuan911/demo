package com.hubidaauto.servmarket.module.common.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.comment.dao.OrderCommentDao;
import com.hubidaauto.servmarket.module.comment.entity.CommentVO;
import com.hubidaauto.servmarket.module.common.annotation.ContentType;
import com.hubidaauto.servmarket.module.common.dao.ImageContentDao;
import com.hubidaauto.servmarket.module.common.dao.TextContentDao;
import com.hubidaauto.servmarket.module.common.entity.ImageContentVO;
import com.hubidaauto.servmarket.module.common.entity.TextContentVO;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/common")
public class CommonWebRouter {
    Logger logger = LoggerFactory.getLogger(CommonWebRouter.class);

    ImageContentDao imageContentDao;

    TextContentDao textContentDao;

    @Autowired
    public void setTextContentDao(TextContentDao textContentDao) {
        this.textContentDao = textContentDao;
    }

    @Autowired
    public void setImageContentDao(ImageContentDao imageContentDao) {
        this.imageContentDao = imageContentDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }

    @VertxRouter(path = "/img",
            method = "POST")
    public void img(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            CompositeFuture
                    .all(routingContext.fileUploads()
                            .stream()
                            .filter(fileUpload -> !fileUpload.cancel())
                            .map(fileUpload ->
                                    routingContext
                                            .vertx()
                                            .fileSystem()
                                            .readFile(fileUpload.uploadedFileName())
                                            .compose(buffer -> {
                                                ImageContentVO imageContentVO = new ImageContentVO();
                                                imageContentVO.setContent(buffer.getBytes());
                                                try {
                                                    imageContentDao.add(imageContentVO);
                                                    return Future.succeededFuture(imageContentVO.getId());
                                                } catch (Throwable e) {
                                                    return Future.failedFuture(e);
                                                } finally {
                                                    routingContext.vertx()
                                                            .fileSystem()
                                                            .delete(fileUpload.uploadedFileName())
                                                            .onFailure(throwable -> {
                                                                logger.error(throwable.getMessage(), throwable);
                                                            });
                                                }
                                            })
                            ).collect(Collectors.toList())
                    ).onComplete(compositeFutureAsyncResult -> {
                CompositeFuture future = (CompositeFuture) compositeFutureAsyncResult;
                List<Object> objects = new LinkedList<>();
                for (int i = 0, len = future.size(); i < len; i++) {
                    if (future.failed(i)) {
                        objects.add(future.cause(i).getMessage());
                    } else {
                        objects.add(future.resultAt(i));
                    }
                }
                routingContext.end(JSONObject.toJSONString(objects));
            });
        });
    }

    @VertxRouter(path = "\\/img\\/(?<id>\\d+)",
            mode = VertxRouteType.PathRegex,
            method = "GET")
    public void getImg(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext
                    .response()
                    .putHeader("Content-Type", "image/png")
                    .end(Buffer.buffer(imageContentDao.get(Long.parseLong(routingContext.pathParam("id"))).getContent()));
        });
    }

    @VertxRouter(path = "\\/imgs\\/(?<type>\\d+)\\/(?<refId>\\d+)",
            mode = VertxRouteType.PathRegex,
            method = "GET")
    public void imgs(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            ImageContentVO imageContentVO = new ImageContentVO();
            imageContentVO.setType(Long.parseLong(routingContext.pathParam("type")));
            imageContentVO.setRefId(Long.parseLong(routingContext.pathParam("refId")));
            try {
                routingContext.end(JSON.toJSONString(imageContentDao.list(imageContentVO)));
            } catch (Throwable e) {
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "/text",
            method = "POST")
    public void text(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            try {
                TextContentVO vo = JSONObject.parseObject(routingContext.getBodyAsString(), TextContentVO.class);
                textContentDao.add(vo);
                if (vo.getImgIds() != null) {
                    for (Long id : vo.getImgIds()) {
                        ImageContentVO image = new ImageContentVO();
                        image.setRefId(vo.getId());
                        image.setType(20006L);
                        image.setId(id);
                        imageContentDao.put(image);
                    }
                }
                routingContext.end(new JsonObject().put("id", vo.getId()).toBuffer());
            } catch (Throwable e) {
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

}
