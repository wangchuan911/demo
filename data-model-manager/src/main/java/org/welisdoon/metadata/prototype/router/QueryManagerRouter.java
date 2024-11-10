package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.condition.Page;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
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
    TransactionTemplate transactionTemplate;
    boolean lazy = false;

    @Autowired
    public void setSqlBuilderHandler(SqlBuilderHandler sqlBuilderHandler) {
        this.sqlBuilderHandler = sqlBuilderHandler;
    }

    @Autowired
    public void setMetaObjectDao(MetaObjectDao metaObjectDao) {
        this.metaObjectDao = metaObjectDao;
    }

    @Autowired
    public void setMetaLinkDao(MetaLinkDao metaLinkDao) {
        this.metaLinkDao = metaLinkDao;
    }

    @Autowired
    public void setMetaAttributeDao(MetaAttributeDao metaAttributeDao) {
        this.metaAttributeDao = metaAttributeDao;
    }

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
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
            SqlContent context = SqlContent.getInstance();
            sqlBuilderHandler.handler(context, metaLinkDao.list(condition).stream().findFirst().orElseGet(() -> {
                repairObjConstructionData(metaObjectDao.get(qid));
                return metaLinkDao.list(condition).stream().findFirst().orElseThrow();
            }));
            routingContext.end(context.toSqlJoin());
        });
    }

    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "PUT",
            mode = VertxRouteType.PathRegex)
    public void objAttrAdd(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaObject.Attribute attribute = JSON.parseObject(routingContext.body().asString()).toJavaObject(MetaObject.Attribute.class);
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
            if (attribute.getId() != null)
                metaAttributeDao.update(attribute);
            else
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
            event.response().setStatusCode(500).end(Optional.ofNullable(event.failure().getMessage()).orElse(""));
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
                    /*MetaLinkCondition condition = new MetaLinkCondition();
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
                    if(result.isEmpty()){*/
                    result.addAll(LinkMetaType.getChildTypeId(LinkMetaType.SqlToJoin.getId()).stream().map(LinkMetaType::getInstance).collect(Collectors.toList()));
                    /*}*/
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
            MetaLink metaLink = JSON.parseObject(routingContext.body().asString()).toJavaObject(MetaLink.class);
            metaLinkDao.add(metaLink);
            routingContext.end(JSON.toJSONString(metaLink));
        });
    }

    @VertxRouter(path = "\\/query\\/object\\/(?<id>\\d+)",
            method = "GET", mode = VertxRouteType.PathRegex)
    public void queryObject(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaObjectCondition condition = new MetaObjectCondition();
            condition.setData(new MetaObject());
            condition.getData().setCode(routingContext.queryParam("text").stream().findFirst().orElse(""));
            condition.setPage(new Page(1, 10));
            condition.setQuery("objectSearch");
            routingContext.end(JSON.toJSONString(
                    metaObjectDao.list(condition).stream().map(metaObject -> ImmutableMap.of("id", metaObject.getId(), "desc", String.format("[%s]%s", metaObject.getName(), metaObject.getCode()))).toArray()));
        });
    }

    @VertxRouter(path = "\\/query\\/link\\/type\\/(?<typeLinkId>\\d+)",
            method = "GET", mode = VertxRouteType.PathRegex)
    public void showObjectSubLinkType(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long typeLinkId = Long.parseLong(routingContext.pathParam("typeLinkId"));
            routingContext.end(JSON.toJSONString(LinkMetaType.getChildTypeId(LinkMetaType.Sql.getId()).stream().map(aLong -> {
                return ImmutableMap.of("id", aLong, "desc", LinkMetaType.getInstance(aLong).getDesc());
            }).toArray()));
        });
    }

    @VertxRouter(path = "\\/add\\/obj\\/link\\/rel\\/(?<objectId>\\d+)",
            method = "POST", mode = VertxRouteType.PathRegex)
    public void objectAddLinkRel(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            transactionTemplate.execute(status -> {
                try {
                    long objectId = Long.parseLong(routingContext.pathParam("objectId"));
                    JSONObject body = JSONObject.parseObject(routingContext.body().asString());

                    Long object = body.getLong("object");
                    Long parent = body.getLong("parent");
                    Long typeId = body.getLong("type");
                    MetaLink link0 = null;
                    boolean start = false, stop = false;
                    for (MetaLink link : getLinks(objectId)) {
                        if (link.getId() < 0) continue;
                        if (start) {
                            if (!stop) {
                                stop = true;
                                link0 = new MetaLink();
                                link0.setObjectId(object);
                                link0.setTypeId(typeId);
                                link0.setInstanceId(getNextInstanceId(objectId));
                                link0.setSequence(link.getSequence());
                                metaLinkDao.add(link0);
                            }
                            link.setSequence(link.getSequence() + 1);
                            metaLinkDao.put(link);
                            continue;
                        } else {
                            start = link.getInstanceId() == parent;
                        }
                    }
                    JSONArray rel = body.getJSONArray("rel");
                    for (int i = 0; i < rel.size(); i++) {
                        objectAddLinkRel(link0, link0, rel.getJSONObject(i));
                    }
                    status.flush();
                } catch (Throwable e) {
                    status.setRollbackOnly();
                }
                return null;
            });

            logger.info(routingContext.body().asString());
            routingContext.end();
        });
    }

    public void objectAddLinkRel(MetaLink root, MetaLink parent, JSONObject link) {

        MetaLink metaLink = new MetaLink();
        metaLink.setTypeId(link.getLong("linkTypeId"));
        metaLink.setAttributeId(link.getLong("attributeId"));
        String instanceIdStr = link.getString("instanceId");
        if (StringUtils.isNotEmpty(instanceIdStr) && StringUtils.isNumeric(instanceIdStr)) {
            metaLink.setInstanceId(Long.parseLong(instanceIdStr));
        } else {
            metaLink.setInstance(root.getInstance());
        }
        metaLink.setParentId(parent.getId());
        metaLink.setObjectId(link.getLong("objectId"));
        metaLinkDao.add(metaLink);
        Optional.ofNullable(link.getJSONArray("children")).ifPresent(objects -> {
            for (int i1 = 0; i1 < objects.size(); i1++) {
                objectAddLinkRel(root, metaLink, objects.getJSONObject(i1));
            }
        });
    }

    protected long getNextInstanceId(long objectId) {
        List<Long> list = getInstanceIds(objectId).stream().sorted().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 2L;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != i + 2) {
                return list.get(i) - 1;
            }
        }
        return list.get(list.size() - 1) + 1;
    }

    protected Collection<Long> getInstanceIds(long objectId) {
        Set<Long> set = new HashSet<>();
        List<MetaLink> list = getLinks(objectId);
        for (MetaLink metaLink : list) {
            set.addAll(getInstanceIds(metaLink));
        }
        return set;
    }

    protected Collection<Long> getInstanceIds(MetaLink metaLink) {
        Set<Long> set = new HashSet<>();
        Long instanceId = metaLink.getInstanceId();
        if (instanceId != null || metaLink.getInstanceId() > 0) {
            for (MetaLink child : metaLink.getChildren()) {
                set.add(child.getInstanceId());
            }
        }
        return set;
    }

    @VertxRouter(path = "/obj",
            method = "PUT")
    public void addObject(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaObject object = JSONObject.parseObject(routingContext.body().asString(), MetaObject.class);
            metaObjectDao.add(object);
            if (object.getType() == ObjectMetaType.Object) {
                MetaLink link = new MetaLink();
                link.setTypeId(LinkMetaType.ObjConstructor.getId());
                link.setObjectId(object.getId());
                link.setParentId(0L);
                metaLinkDao.add(link);
            }
            routingContext.end(JSON.toJSONString(object));
        });
    }

    protected void repairObjConstructionData(MetaObject object) {
        if (object.getType() == ObjectMetaType.Object) {
            MetaLink link = new MetaLink();
            link.setTypeId(LinkMetaType.ObjConstructor.getId());
            link.setObjectId(object.getId());
            link.setParentId(0L);
            metaLinkDao.add(link);
        }
    }

    @VertxRouter(path = "/obj/type",
            method = "GET")
    public void getObjectType(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(JSON.toJSONString(Stream.of(ObjectMetaType.Object, ObjectMetaType.Table)
                    .map(objectMetaType -> ImmutableMap.of("id", objectMetaType.getId(), "desc", objectMetaType.getDesc())).toArray()));
        });
    }

    @VertxRouter(path = "\\/obj(?<objectId>\\d+)\\/parent(?<parentId>\\d+)",
            method = "POST", mode = VertxRouteType.PathRegex)
    public void getObjectParent(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            MetaObject object = new MetaObject();
            object.setId(Long.parseLong(routingContext.pathParam("objectId")));
            object.setParentId(Long.parseLong(routingContext.pathParam("parentId")));
            routingContext.end(Integer.toString(metaObjectDao.put(object)));
        });
    }

    @VertxRouter(path = "\\/obj\\/(?<id>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void objDel(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaObject object = metaObjectDao.get(qid);
            Assert.isTrue(org.apache.commons.collections4.CollectionUtils.isEmpty(object.getChildren()), "有子类不能删除");
            for (MetaObject.Attribute attribute : object.getAttributes()) {
                metaAttributeDao.delete(attribute.getId());
            }
            MetaLinkCondition condition = new MetaLinkCondition();
            condition.setData(new MetaLink());
            condition.getData().setObjectId(object.getId());
            condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());

            metaLinkDao.list(condition).stream().forEach(metaLink -> {
                delLink(metaLink);
            });
            metaObjectDao.delete(qid);
            routingContext.end();
        });
    }

    protected int delLink(MetaLink metaLink) {
        if (Objects.isNull(metaLink) || Objects.isNull(metaLink.getId())) {
            return 0;
        }
        (Optional.ofNullable(metaLink.getChildren()).orElse(Collections.emptyList())).stream().filter(Objects::nonNull).forEach(metaLink1 -> {
            delLink(metaLink1);
        });
        return metaLinkDao.delete(metaLink.getId());
    }

    ;
}
