package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.handle.HandleContext;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlBuilderHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    SqlBuilderHandler sqlBuilderHandler;
    boolean lazy = false;

    @Autowired
    public void setSqlBuilderHandler(SqlBuilderHandler sqlBuilderHandler) {
        this.sqlBuilderHandler = sqlBuilderHandler;
    }

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
            routingContext.end(formatLinkToString(getLinks(Long.parseLong(routingContext.pathParam("id")))));
        });
    }

    protected JSONObject linkToJson(MetaLink metaLink) {
        JSONObject object = (JSONObject) JSON.toJSON(metaLink);
        object.put("value", metaLink.getValue());
        object.put("object", metaLink.getObject());
        object.put("instance", metaLink.getInstance());
        object.put("attribute", metaLink.getAttribute());

        MetaLinkCondition condition = new MetaLinkCondition();
        List<MetaLink> list = new LinkedList<>();
        switch (metaLink.getId() < 0 ? "obj" : "attr") {
            case "obj":
                list.addAll(getLinks(metaLink.getObjectId()));
                break;
            default:
                condition.setData(new MetaLink());
                condition.setParentId(metaLink.getId());
                list.addAll(metaLinkDao.list(condition));
                break;
        }
        if (!lazy) {
            if (!CollectionUtils.isEmpty(list)) {
                object.put("children", list.stream().map(this::linkToJson).collect(Collectors.toList()));
            }
        } else {
            object.put("hasChildren", !CollectionUtils.isEmpty(list));
        }

        return object;
    }

    protected String formatLinkToString(List<MetaLink> list) {
        return JsonUtils.asJsonString(list
                .stream().map(this::linkToJson).collect(Collectors.toList()));
    }

    protected List<MetaLink> getLinks(long qid) {
        List<MetaLink> list = new LinkedList<>();
        Optional.ofNullable(metaObjectDao.get(qid).getParentId()).ifPresent(aLong -> {
            MetaLink metaLink = new MetaLink();
            metaLink.setObjectId(aLong);
            metaLink.setId(-1 * qid);
            metaLink.setTypeId(LinkMetaType.ObjConstructor.getId());
            metaLink.setInstanceId(1L);
            list.add(metaLink);
        });

        MetaLinkCondition condition = new MetaLinkCondition();
        condition.setData(new MetaLink());
        condition.getData().setObjectId(qid);
        condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());
        list.addAll(metaLinkDao.list(condition).stream().flatMap(metaLink -> {
            return LinkMetaType.getChildTypeId(LinkMetaType.ObjConstructor.getId()).stream().flatMap(aLong -> {
                return LinkMetaType.getChildTypeId(aLong).stream();
            }).flatMap(aLong -> {
                MetaLinkCondition condition1 = new MetaLinkCondition();
                condition1.setData(new MetaLink());
                condition1.setParentId(metaLink.getId());
                condition1.getData().setTypeId(aLong);
                return metaLinkDao.list(condition1).stream();
            });
        }).collect(Collectors.toList()));
        return list;
    }

    @VertxRouter(path = "\\/link\\/expand\\/(?<type>\\w*)(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void linkExpand(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaLinkCondition condition = new MetaLinkCondition();
            List<MetaLink> list = new LinkedList<>();
            long qid = Long.parseLong(routingContext.pathParam("id"));
            switch (Optional.ofNullable(routingContext.pathParam("type")).orElse("")) {
                case "obj":
                    list.addAll(getLinks(qid));
                    break;
                default:
                    condition.setData(new MetaLink());
                    condition.setParentId(qid);
                    list.addAll(metaLinkDao.list(condition));
                    break;
            }
            routingContext.end(formatLinkToString(list));
        });
    }

    @VertxRouter(path = "\\/link\\/show\\/(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void show(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaLinkCondition condition = new MetaLinkCondition();
            condition.setData(new MetaLink());
            condition.getData().setObjectId(qid);
            condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());
            HandleContext context = new HandleContext();
            sqlBuilderHandler.handler(context, metaLinkDao.list(condition).get(0));
            routingContext.end(((SqlContent) context.get(sqlBuilderHandler)).toSqlJoin());
        });
    }

}
