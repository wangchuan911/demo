package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname QueryManagerRouter
 * @Description TODO
 * @Author Septem
 * @Date 16:13
 */
@Component
@VertxConfiguration
@VertxRoutePath(prefix = "/md", requestBodyEnable = true)
public class QueryManagerRouter {
    MetaObjectDao metaObjectDao;
    MetaLinkDao metaLinkDao;

    @Autowired
    public void setMember(MetaLinkDao metaLinkDao, MetaObjectDao metaObjectDao) {
        this.metaLinkDao = metaLinkDao;
        this.metaObjectDao = metaObjectDao;
    }

    @VertxRouter(path = "\\/obj\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void findObj(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.valueOf(routingContext.pathParam("id"));
            MetaObject object = metaObjectDao.get(qid);
            JSONObject jsonObject = (JSONObject) JSON.toJSON(object);
            jsonObject.put("parent", object.getParent());
            routingContext.end(jsonObject.toJSONString());
        });
    }

    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void objAttr(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaObject object = new MetaObject();
            object.setId(qid);
            routingContext.end(JSON.toJSONString(object.getAttributes()));
        });
    }

    @VertxRouter(path = "/obj", method = "POST")
    public void objQuery(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaObjectCondition condition = JsonUtils.toBean(routingContext.body().asString(), MetaObjectCondition.class);
            condition.startPage();
            routingContext.end(JsonUtils.asJsonString(PageInfo.of(metaObjectDao.list(condition))));
        });
    }

    @VertxRouter(path = "\\/obj\\/combination\\/(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void objComponent(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaLinkCondition condition = new MetaLinkCondition();
            condition.setData(new MetaLink());
            condition.getData().setObjectId(qid);
            condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());
            routingContext.end(JsonUtils.asJsonString(metaLinkDao.list(condition).stream().flatMap(metaLink -> {
                MetaLinkCondition condition1 = new MetaLinkCondition();
                condition1.setData(new MetaLink());
                condition1.setParentId(metaLink.getId());
                condition1.getData().setTypeId(LinkMetaType.SqlToJoin.getId());
                return metaLinkDao.list(condition1).stream();
            }).map(metaLink -> {
                JSONObject object = (JSONObject) JSON.toJSON(metaLink);
                object.put("value", metaLink.getValue());
                object.put("object", metaLink.getObject());
                object.put("instance", metaLink.getInstance());
                object.put("attribute", metaLink.getAttribute());
                return object;
            }).collect(Collectors.toList())));
        });
    }

}
