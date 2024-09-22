package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final Logger logger = LoggerFactory.getLogger(QueryManagerRouter.class);

    MetaObjectDao metaObjectDao;
    MetaLinkDao metaLinkDao;
    MetaAttributeDao metaAttributeDao;
    SqlBuilderHandler sqlBuilderHandler;
    boolean lazy = false;

    @Autowired
    public void setSqlBuilderHandler(SqlBuilderHandler sqlBuilderHandler) {
        this.sqlBuilderHandler = sqlBuilderHandler;
    }

    @Autowired
    public void setMember(MetaLinkDao metaLinkDao, MetaObjectDao metaObjectDao, MetaAttributeDao metaAttributeDao) {
        this.metaLinkDao = metaLinkDao;
        this.metaObjectDao = metaObjectDao;
        this.metaAttributeDao = metaAttributeDao;
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

    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "PUT",
            mode = VertxRouteType.PathRegex)
    public void objAttrAdd(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaObject.Attribute attribute = routingContext.body().asPojo(MetaObject.Attribute.class);
            attribute.setObjectId(qid);
            switch (MetaUtils.getInstance().getObject(qid).getType()) {
                case Object:
                    attribute.setTypeId(AttributeMetaType.Attributes.getId());
                    break;
                case Table:
                    attribute.setTypeId(AttributeMetaType.Column.getId());
                    break;
                default:
                    routingContext.response().setStatusCode(500).end(String.format("不支持的对象类型[%s]", MetaUtils.getInstance().getObject(qid).getType().getDesc()));
                    return;
            }
            Assert.notNull(attribute.getObjectId(), "not object");
            Assert.notNull(attribute.getCode(), "not code");
            Assert.notNull(attribute.getName(), "not name");
            metaAttributeDao.add(attribute);
            routingContext.end(JSON.toJSONString(attribute));
        });
    }

    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void objAttrDel(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            metaAttributeDao.delete(qid);
            routingContext.end();
        });
    }

    @VertxRouter(path = "*")
    public void error(RoutingContextChain chain) {
        chain.failureHandler(event -> {
            logger.error(event.failure().getMessage(), event.failure());
            event.response().setStatusCode(500).end(event.failure().getMessage());
        });
    }

    @VertxRouter(path = "\\/link\\/types\\/(?<type>\\w*)(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void linkTypes(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            Long id = TypeUtils.castToJavaBean(routingContext.pathParam("id"), Long.class);
            String type = TypeUtils.castToJavaBean(routingContext.pathParam("type"), String.class);
            List<LinkMetaType> result = new LinkedList<>();
            switch (Optional.ofNullable(type).orElse("")) {
                case "obj":
                    MetaLinkCondition condition = new MetaLinkCondition();
                    condition.setData(new MetaLink());
                    condition.getData().setObjectId(id);
                    condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());
                    result.addAll(metaLinkDao.list(condition).stream().flatMap(metaLink -> {
                        switch (metaLink.getType()) {
                            case ObjToDataBase:
                                return LinkMetaType.getChildTypeId(LinkMetaType.SqlToJoin.getId()).stream().map(LinkMetaType::getInstance);
                            default:
                                return Stream.of();
                        }
                    }).collect(Collectors.toList()));
                    break;
                case "link":
                    MetaLink metaLink = metaLinkDao.get(id);

                    break;
                default:
                    break;
            }
            routingContext.end(JSON.toJSONString(result.stream()
                    .map(linkMetaType -> Map.of("id", linkMetaType.getId(), "desc", linkMetaType.getDesc())).toArray()));
        });
    }

    @VertxRouter(path = "/link",
            method = "PUT")
    public void linkAdd(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaLink metaLink = routingContext.body().asPojo(MetaLink.class);
            metaLinkDao.add(metaLink);
            routingContext.end(JSON.toJSONString(metaLink));
        });
    }
}
