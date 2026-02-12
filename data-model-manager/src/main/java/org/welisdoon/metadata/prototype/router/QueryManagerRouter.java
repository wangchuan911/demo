package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import com.github.pagehelper.PageInfo;
import com.hazelcast.shaded.org.jctools.queues.MessagePassingQueue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.welisdoon.common.JsonUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.condition.Page;
import org.welisdoon.metadata.prototype.consts.*;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.*;
import org.welisdoon.metadata.prototype.entity.DataObject;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.TemplateFormatContent;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.XmlTemplateFormatContent;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.QueryTemplateInstance;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.VertxSqlDataBasePool;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.FormatContent;
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
    TransactionTemplate transactionTemplate;
    VertxSqlDataBasePool vertxSqlDataBasePool;
//    @Value("${md.lazy:false}")
//    boolean lazy = false;

    //    @Autowired
//    public void setSqlBuilderHandler(SqlBuilderHandler sqlBuilderHandler) {
//        this.sqlBuilderHandler = sqlBuilderHandler;
//    }
    public QueryManagerRouter(MetaUtils metaUtils, TransactionTemplate transactionTemplate, VertxSqlDataBasePool vertxSqlDataBasePool) {
        this.metaObjectDao = metaUtils.getMetaObjectDao();
        this.metaLinkDao = metaUtils.getMetaLinkDao();
        this.metaAttributeDao = metaUtils.getMetaAttributeDao();
        this.transactionTemplate = transactionTemplate;
        this.vertxSqlDataBasePool = vertxSqlDataBasePool;
    }

    /*@Autowired
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
    }*/


    @VertxRouter(path = "\\/obj\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void findObj(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
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
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            routingContext.end(JSON.toJSONString(MetaUtils.getInstance().getMetaAttributeDao().list(new MetaObject.Attribute().setObjectId(qid))));
        });
    }

    @VertxRouter(path = "/obj", method = "POST")
    public void objQuery(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            MetaObjectCondition condition = JsonUtils.toBean(routingContext.body().asString(), MetaObjectCondition.class);
            routingContext.end(JsonUtils.asJsonString(PageInfo.of(metaObjectDao.list(condition))));
        });
    }

    @VertxRouter(path = "\\/obj\\/combination\\/(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void objComponent(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(formatLinkToString(getLinks(Long.parseLong(routingContext.pathParam("id")))));
        });
    }

    protected JSONObject linkToJson(MetaLink metaLink) {
        JSONObject object = (JSONObject) JSON.toJSON(metaLink);
        object.put("value", metaLink.getValue());
        object.put("object", metaLink.getObject());
        object.put("instance", metaLink.getInstance());
        object.put("attribute", metaLink.getAttribute());

        List<MetaLink> list = new LinkedList<>();
        switch (metaLink.getId() < 0 ? "obj" : "attr") {
            case "obj":
                list.addAll(getLinks(metaLink.getObjectId()));
                break;
            default:
                list.addAll(metaLink.getChildren());
                break;
        }
//        if (!lazy) {
        if (!CollectionUtils.isEmpty(list)) {
            object.put("children", list.stream().map(this::linkToJson).collect(Collectors.toList()));
        }
//        } else {
//            object.put("hasChildren", !CollectionUtils.isEmpty(list));
//        }

        return object;
    }

    protected String formatLinkToString(List<MetaLink> list) {
        return JsonUtils.asJsonString(list
                .stream().map(this::linkToJson).collect(Collectors.toList()));
    }

    protected List<MetaLink> getLinks(long qid) {
        /*List<MetaLink> list = new LinkedList<>();
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
        return list;*/
        MetaObject object = MetaUtils.getInstance().getObject(qid);
        if (object instanceof DataObject) {
            return ((DataObject) object).getConstructorLinks();
        }
        return Collections.emptyList();
    }

    @VertxRouter(path = "\\/link\\/expand\\/(?<type>[a-zA-Z]*)(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void linkExpand(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
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
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            routingContext.end(new org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent(MetaUtils.getInstance().getObject(qid)).format(new FormatContent()));
//            SqlContent context = new SqlContent();
//            routingContext.end(Optional.ofNullable(MetaUtils.getInstance().<MetaObject>getObject(qid)).map(MetaObject::getConstruct).map(construct -> {
//                sqlBuilderHandler.handler(context, construct);
//                return sqlShowBuilder.build(context);
//            }).orElse("未配置关联,无法生成展示!"));
        });
    }

    @VertxRouter(path = "\\/link\\/template\\/(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void getTemplate(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            TemplateFormatContent templateFormatContent = new XmlTemplateFormatContent();
            new org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent(MetaUtils.getInstance().getObject(qid)).format(templateFormatContent);
            templateFormatContent.build();
            routingContext.response().putHeader("content-type", "application/xml").end(templateFormatContent.getValue().toString());
//            SqlContent context = new SqlContent();
//            routingContext.end(Optional.ofNullable(MetaUtils.getInstance().<MetaObject>getObject(qid)).map(MetaObject::getConstruct).map(construct -> {
//                sqlBuilderHandler.handler(context, construct);
//                return sqlShowBuilder.build(context);
//            }).orElse("未配置关联,无法生成展示!"));
        });
    }

    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "PUT",
            mode = VertxRouteType.PathRegex)
    public void objAttrAdd(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(JSON.toJSONString(
                    transactionTemplate.execute(status -> {
                        long qid = Long.parseLong(routingContext.pathParam("id"));
                        JSONObject attrJSON = JSON.parseObject(routingContext.body().asString());

                        MetaObject metaObject = MetaUtils.getInstance().getObject(qid);
                        MetaObject.Attribute attribute = attrJSON.toJavaObject(((MetaObject.Attribute) MetaUtils.getInstance().getType(Optional.ofNullable(attrJSON.getLong("typeId")).map(MetaUtils.getInstance()::getMetaType).orElseGet(() -> {
                            if (metaObject instanceof DataObject) {
                                return AttributeMetaType.Field;
                            } else {
                                return AttributeMetaType.Column;
                            }
                        }))).getClass());
                        attribute.setObjectId(qid);
                        Arrays.stream(AttributeMetaType.values()).filter(attributeMetaType -> attributeMetaType.getObjectMetaType() == metaObject.getType()).findFirst().ifPresentOrElse(attributeMetaType -> {
                            attribute.setTypeId(attributeMetaType.getId());
                        }, () -> {
                            throw new IllegalStateException(String.format("不支持的对象类型[%s]", metaObject.getType().getDesc()));
                        });
                        Assert.notNull(attribute.getObjectId(), "not object");
                        Assert.notNull(attribute.getCode(), "not code");
                        Assert.notNull(attribute.getName(), "not name");
                        if (attribute instanceof DataObject.Field) {
//                            MetaList<MetaLink> exists = ((DataObject.Field) attribute).getColumnLinks();
//                            MetaProtoList<MetaLink> create = new MetaProtoList<>();
                            String attr = attrJSON.getString("attr");
                            logger.info("处理parentId");
                            ListIterator<MetaPrototype> iterator = getAttrPath(attr, metaObject).listIterator();
                            MetaLink root = null, prev = null, next = null;


                            MetaPrototype metaPrototype;
                            if (iterator.hasNext()) {
                                metaPrototype = iterator.next();
                                if (metaPrototype instanceof MetaLink) {
                                    root = next = new MetaLink().setLinkId(metaPrototype.getId())/*
                                            .setObjectId(((MetaLink) metaPrototype).getObjectId())
                                            .setAttributeId(((MetaLink) metaPrototype).getAttributeId())
                                            .setSequence(((MetaLink) metaPrototype).getSequence())
                                            .setInstanceId(((MetaLink) metaPrototype).getInstanceId())
                                            .setValueId(((MetaLink) metaPrototype).getValueId())*/;
                                } else if (metaPrototype instanceof MetaObject.Attribute) {
                                    root = next = new MetaLink().setObjectId(((MetaObject.Attribute) metaPrototype).getObjectId()).setAttributeId(metaPrototype.getId());
                                }
                                root.<MetaLink>setTypeId(LinkMetaType.SqlToSelect.getId());
                                prev = next;
                            }
                            while (iterator.hasNext()) {
                                metaPrototype = iterator.next();
                                if (metaPrototype instanceof MetaLink) {
                                    next = new MetaLink()
                                            .<MetaLink>setTypeId(LinkMetaType.SqlToSelect.getId())
                                            .setLinkId(metaPrototype.getId())/*
                                            .setObjectId(((MetaLink) metaPrototype).getObjectId())
                                            .setAttributeId(((MetaLink) metaPrototype).getAttributeId())
                                            .setSequence(((MetaLink) metaPrototype).getSequence())
                                            .setInstanceId(((MetaLink) metaPrototype).getInstanceId())
                                            .setValueId(((MetaLink) metaPrototype).getValueId())*/;
                                } else if (metaPrototype instanceof MetaObject.Attribute) {
                                    prev.setAttributeId(metaPrototype.getId());
                                    Assert.isTrue(!iterator.hasNext(), "错误的类型");
                                    break;
                                }
                                prev.setChildren(MetaProtoList.of(next));
                                prev = next;
                            }

                            /*for (; iterator.hasNext(); ) {
                                MetaPrototype metaPrototype = iterator.next();
                                switch (iterator.hasPrevious() ? 0 : (iterator.hasNext() ? 1 : 2)) {
                                    case 0:
                                        if (metaPrototype instanceof MetaLink) {
                                            long linkId = metaPrototype.getId() < 0 ? metaObject.getConstructId() : metaPrototype.getId();
                                            root = next = (new MetaLink().setTypeId(LinkMetaType.SqlToSelect.getId()).<MetaLink>setParentId(linkId).setObjectId(((MetaLink) metaPrototype).getObjectId()).setInstanceId(((MetaLink) metaPrototype).getInstanceId()).setSequence(1));
                                        } else if (metaPrototype instanceof MetaObject.Attribute) {
                                            root = next = (new MetaLink().setTypeId(LinkMetaType.SqlToSelect.getId()).<MetaLink>setParentId(metaObject.getConstructId()).setObjectId(((MetaObject.Attribute) metaPrototype).getObjectId()).setInstanceId(((MetaLink) metaPrototype).getInstanceId()));
                                        }
                                        break;
                                    case 1:
                                        Assert.isTrue(metaPrototype instanceof MetaLink, "错误的类型");
                                        next = (new MetaLink().<MetaLink>setTypeId(LinkMetaType.SqlToSelect.getId()).setObjectId(((MetaLink) metaPrototype).getObjectId()).setInstanceId(((MetaLink) metaPrototype).getInstanceId()));
                                        break;
                                    case 2:
                                        Assert.isTrue(metaPrototype instanceof MetaObject.Attribute, "错误的类型");
                                        prev.setAttributeId(metaPrototype.getId());
                                        break;
                                    default:
                                        throw new IllegalArgumentException("错误的类型");
                                }
                                if (prev != null) {
                                    prev.setChildren(MetaProtoList.of(next));
                                }
                                prev = next;
                            }*/
                            ((DataObject.Field) attribute).setColumn(root);

                            logger.info("处理mapper");
                            JSONArray rows = JsonUtils.getKeyValueToBean(attrJSON, "colMapper.rows", JSONArray.class);
                            JSONArray cols = JsonUtils.getKeyValueToBean(attrJSON, "colMapper.cols", JSONArray.class);
                            JSONArray del = JsonUtils.getKeyValueToBean(attrJSON, "colMapper.dels", JSONArray.class);
                            if (del != null) {
                                ((DataObject.Field) attribute).getForeignKey().del(del.toJavaList(String[].class));
                            }
                            /*MetaProtoList<MetaLink> rowsOfLink = new MetaProtoList<>();
                            rowsOfLink.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Col.getId()).setAttributeId(attribute.getId()).setSequence(0));
                            for (int i1 = 0; i1 < cols.size(); i1++) {
                                Long selfColAttrId = JsonUtils.getKeyValueToBean(cols.getJSONObject(i1), "attrId", Long.class);
                                rowsOfLink.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Col.getId()).setAttributeId(selfColAttrId).setSequence(i1 + 1));
                            }
                            for (int i = 0; i < rows.size(); i++) {
                                MetaProtoList<MetaLink> cellOfLink = new MetaProtoList<>();
                                JSONObject mapper = JsonUtils.getKeyValueToBean(rows.getJSONObject(i), "mapper", JSONObject.class);
                                Long outObjectId = JsonUtils.getKeyValueToBean(rows.getJSONObject(i), "objectId", Long.class);
                                Long outCurrentAttrId = mapper.getLong("current");
                                cellOfLink.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Cell.getId()).setAttributeId(outCurrentAttrId).setObjectId(outObjectId).setSequence(0));
                                for (int i1 = 0; i1 < cols.size(); i1++) {
                                    Long outRowAttrId = mapper.getLong(String.valueOf(i1));
                                    cellOfLink.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Cell.getId()).setAttributeId(outRowAttrId).setObjectId(outObjectId).setSequence(i1 + 1));
                                }
                                rowsOfLink.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Row.getId()).<MetaLink>setChildren(cellOfLink).setSequence(i));
                            }*/

                            /*List<MetaLink> colLinks = new LinkedList<>();
                            colLinks.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Col.getId()).setAttributeId(attribute.getId()).<MetaLink>addChildren(Stream.of(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Head.getId()).setAttributeId(attribute.getId()).setSequence(0))).setSequence(0));
                            for (int i1 = 0; i1 < cols.size(); i1++) {
                                Long selfColAttrId = JsonUtils.getKeyValueToBean(cols.getJSONObject(i1), "attrId", Long.class);
                                colLinks.add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Col.getId()).setAttributeId(attribute.getId()).<MetaLink>addChildren(Stream.of(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Head.getId()).setAttributeId(selfColAttrId).setSequence(i1 + 1))).setSequence(i1 + 1));
                            }
                            for (int i = 0; i < rows.size(); i++) {
                                JSONObject mapper = JsonUtils.getKeyValueToBean(rows.getJSONObject(i), "mapper", JSONObject.class);
                                Long outObjectId = JsonUtils.getKeyValueToBean(rows.getJSONObject(i), "objectId", Long.class);
                                Long outCurrentAttrId = mapper.getLong("current");
                                colLinks.get(0).getChildren().add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Cell.getId()).setAttributeId(outCurrentAttrId).setObjectId(outObjectId).setSequence(i));
                                for (int i1 = 0; i1 < cols.size(); i1++) {
                                    Long outRowAttrId = mapper.getLong(String.valueOf(i1));
                                    colLinks.get(i1 + 1).getChildren().add(new MetaLink().<MetaLink>setTypeId(LinkMetaType.Cell.getId()).setAttributeId(outRowAttrId).setObjectId(outObjectId).setSequence(i));
                                }
                            }*/
                            List<DataObject.Col> colLinks = DataObject.Col.append((DataObject.Field) attribute, rows, cols);
                            ((DataObject.Field) attribute).getForeignKey().append(colLinks);
                        }
                        attribute.save();
                        return attribute;
                    })
            ));
        });
    }


    @VertxRouter(path = "\\/obj\\/attrs\\/(?<id>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void objAttrDel(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            metaAttributeDao.delete(qid);
            routingContext.end();
        });
    }

    @VertxRouter(path = "*")
    public void error(RoutingContextChain chain) {
        chain.failureHandler(event -> {
            logger.error(event.failure().getMessage(), event.failure());
            event.response().setStatusCode(500).end(event.failure().getMessage() + "");
        });
    }

    @VertxRouter(path = "\\/link\\/types\\/(?<type>[a-zA-Z]*)(?<id>\\d+)", method = "get", mode = VertxRouteType.PathRegex)
    public void linkTypes(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
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
        chain.blockingHandler(routingContext -> {
            MetaLink metaLink = JSON.parseObject(routingContext.body().asString()).toJavaObject(MetaLink.class);
            metaLinkDao.add(metaLink);
            routingContext.end(JSON.toJSONString(metaLink));
        });
    }

    @VertxRouter(path = "\\/query\\/object\\/(?<id>\\d+)",
            method = "GET", mode = VertxRouteType.PathRegex)
    public void queryObject(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            MetaObjectCondition condition = new MetaObjectCondition();
            condition.setData(new MetaObject());
            condition.getData().setCode(routingContext.queryParam("text").stream().findFirst().orElse(""));
            condition.setPage(new Page(1, 10));
            condition.setQuery("objectSearch");
            routingContext.end(JSON.toJSONString(
                    metaObjectDao.list(condition).stream().map(metaObject -> Map.of("id", metaObject.getId(), "desc", String.format("[%s]%s", metaObject.getName(), metaObject.getCode()))).toArray()));
        });
    }

    @VertxRouter(path = "\\/query\\/link\\/type\\/(?<typeLinkId>\\d+)",
            method = "GET", mode = VertxRouteType.PathRegex)
    public void showObjectSubLinkType(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long typeLinkId = Long.parseLong(routingContext.pathParam("typeLinkId"));
            routingContext.end(JSON.toJSONString(LinkMetaType.getChildTypeId(LinkMetaType.SqlOperator.getId()).stream().map(aLong -> {
                return Map.of("id", aLong, "desc", LinkMetaType.getInstance(aLong).getDesc());
            }).toArray()));
        });
    }

    @VertxRouter(path = "\\/add\\/obj\\/link\\/rel\\/(?<objectId>\\d+)",
            method = "POST", mode = VertxRouteType.PathRegex)
    public void objectAddLinkRel(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long objectId = Long.parseLong(routingContext.pathParam("objectId"));
            JSONObject body = JSONObject.parseObject(routingContext.body().asString());

            Long object = body.getLong("object");
            Long parent = body.getLong("parent");
            Long typeId = body.getLong("type");
                    /*MetaLink link0 = null;
                    boolean start = false, stop = false;
                    List<MetaLink> links = getLinks(objectId);
                    for (MetaLink link : links) {
                        if (link.getId() < 0) continue;
                        if (!start) {
                            start = Objects.equals(link.getInstanceId(), parent);
                            continue;
                        }
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
                    }
                    JSONArray rel = body.getJSONArray("rel");
                    if (link0 == null) {
                        logger.info("初始化数据");
                        MetaObject metaObject = MetaUtils.getInstance().getObject(objectId);
                        Assert.notNull(metaObject, () -> MessageFormat.format("对象{0}不存在", objectId));
                        MetaLink root = metaObject.getConstruct();
                        if (root == null) {
                            root = repairObjConstructionData(new MetaLink().setObjectId(objectId).setParentId(0L).setTypeId(LinkMetaType.ObjToDataBase.getId()));
                            metaObject.setConstructId(root.getId());
                            MetaUtils.getInstance().getMetaObjectDao().put(metaObject);
                        }
                        link0 = repairObjConstructionData(new MetaLink().setObjectId(object).<MetaLink>setTypeId(typeId).<MetaLink>setParentId(root.getId()).setInstanceId(getNextInstanceId(objectId)).setSequence(1));
                    }
                    for (int i = 0; i < rel.size(); i++) {
                        objectAddLinkRel(link0, link0, rel.getJSONObject(i));
                    }
                    status.flush();*/
            MetaObject object1 = MetaUtils.getInstance().getObject(objectId);
            MetaLink current = null;
            if (CollectionUtils.isEmpty(object1.getConstruct().getChildren())) {
                object1.getConstruct().getChildren()
                        .add(current = new MetaLink().setObjectId(object).<MetaLink>setTypeId(typeId).setInstanceId(getNextInstanceId(objectId)).setSequence(1));
            } else {
                ListIterator<MetaLink> iterator = new MirrorList<>(object1.getConstruct().getChildren().stream().filter(metaLink -> metaLink.getType().getParent() == LinkMetaType.SqlToJoin).collect(Collectors.toList()), object1.getConstruct().getChildren()).listIterator();
                while (iterator.hasNext()) {
                    if (Objects.equals(iterator.next().getInstanceId(), parent)) {
                        iterator.add(current = new MetaLink().setObjectId(object).<MetaLink>setTypeId(typeId).setInstanceId(getNextInstanceId(objectId)).setSequence(iterator.nextIndex()));
                        break;
                    }
                }
                while (iterator.hasNext()) {
                    iterator.next().setInstanceId((long) iterator.nextIndex());
                }
            }
            Assert.notNull(current, "初始化失败");
            if (typeId == 3012) {
                String multiName = body.getString("multiName");
                Assert.isTrue(StringUtils.isNotEmpty(multiName), "[多对一节点别名]不能为空!");
                MetaObject.Attribute attribute = new MetaObject.Attribute().setCode(multiName).setName(multiName).setTypeId(AttributeMetaType.MultiField.getId());
                current.setAttribute(attribute);
            }
            JSONArray rel = body.getJSONArray("rel");
            for (int i = 0; i < rel.size(); i++) {
                objectAddLinkRel(current.getInstanceId(), current, rel.getJSONObject(i));
            }

            transactionTemplate.execute(status -> object1.save());

            logger.info("{}", object1);
            logger.info(routingContext.body().asString());
            routingContext.end();
        });
    }

    public void objectAddLinkRel(Long instanceId, MetaLink parent, JSONObject link) {

        MetaLink metaLink = new MetaLink();
        metaLink.setTypeId(link.getLong("linkTypeId"));
        metaLink.setAttributeId(link.getLong("attributeId"));
        String instanceIdStr = link.getString("instanceId");
        metaLink.setParentId(parent.getId());
        metaLink.setObjectId(link.getLong("objectId"));
        if (StringUtils.isNotEmpty(instanceIdStr)) {
            metaLink.setInstanceId(StringUtils.isNumeric(instanceIdStr) ? Long.parseLong(instanceIdStr) : instanceId);
        }
        parent.getChildren().add(metaLink);
        Optional.ofNullable(link.getJSONArray("children")).ifPresent(objects -> {
            for (int i1 = 0; i1 < objects.size(); i1++) {
                objectAddLinkRel(instanceId, metaLink, objects.getJSONObject(i1));
            }
        });
    }

    protected long getNextInstanceId(long objectId) {
        List<Long> list = getLinks(objectId).stream().map(MetaLink::getInstanceId).filter(Objects::nonNull).sorted().collect(Collectors.toList());
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) == (i + 1)) {
                continue;
            }
            return list.get(i - 1) + 1;
        }
        return list.stream().min(Comparator.reverseOrder()).orElse(1L) + 1;
    }


    @VertxRouter(path = "/obj",
            method = "PUT")
    public void addObject(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
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

    protected MetaLink repairObjConstructionData(MetaLink link) {
        return metaLinkDao.list(new MetaLinkCondition().setData(link)).stream().findFirst().orElseGet(() -> {
            metaLinkDao.add(link);
            return link;
        });
    }

    @VertxRouter(path = "/obj/type",
            method = "GET")
    public void getObjectType(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(JSON.toJSONString(Stream.of(ObjectMetaType.Object, ObjectMetaType.Table)
                    .map(objectMetaType -> Map.of("id", objectMetaType.getId(), "desc", objectMetaType.getDesc())).toArray()));
        });
    }

    @VertxRouter(path = "\\/obj(?<objectId>\\d+)\\/parent(?<parentId>\\d+)",
            method = "POST", mode = VertxRouteType.PathRegex)
    public void getObjectParent(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
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
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            MetaObject object = metaObjectDao.get(qid);
            Assert.isTrue(org.apache.commons.collections4.CollectionUtils.isEmpty(object.getChildren()), "有子类不能删除");
            for (MetaObject.Attribute attribute : object.getAttributes()) {
                metaAttributeDao.delete(attribute.getId());
            }
            delLink(object.getConstruct());
            metaObjectDao.delete(qid);
            routingContext.end();
        });
    }


    protected int delLink(MetaLink metaLink) {
        if (Objects.isNull(metaLink) || Objects.isNull(metaLink.getId())) {
            return 0;
        }
        (Optional.ofNullable(metaLink.getChildren()).orElse(MetaProtoList.emptyList())).stream().filter(Objects::nonNull).forEach(metaLink1 -> {
            delLink(metaLink1);
        });
        return metaLinkDao.delete(metaLink.getId());
    }

    @VertxRouter(path = "\\/attr\\/bind\\/tree\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void attrBindTree(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long qid = Long.parseLong(routingContext.pathParam("id"));
            List<Map<String, Object>> list = getLinks(qid).stream().map(metaLink -> {
                return attrBindTree(null, metaLink, metaLink, null);
            }).filter(MapUtils::isNotEmpty).collect(Collectors.toList());
            routingContext.end(JSON.toJSONString(list));
        });
    }

    protected Map<String, Object> attrBindTree(MetaObject parent, MetaLink link, final MetaLink root, String seq) {
        if (link == null || link.getObject() == null) {
            return null;
        }
        String seqNext = (StringUtils.isNotEmpty(seq) ? (seq + ",") : "[") + path(link);
        switch (link.getObject().getType()) {
            case Object:
                List<MetaLink> list2 = getLinks(link.getObject().getId());
                return toTreeNode(link.getObject(), list2.stream().map(metaLink -> {
                    return attrBindTree(link.getObject(), metaLink, root, seqNext);
                }).collect(Collectors.toList()), entries -> {
                    entries.add(Map.entry("seq", seqNext));
                    entries.add(Map.entry("instanceId", link.getInstanceId()));
                });
            case Table:
                return toTreeNode(link.getObject(), link.getObject().getAttributes().stream().map(attribute -> {
                    return toTreeNode(attribute, null, entries -> {
                        if (root != null)
                            entries.add(Map.entry("rootInstanceId", root.getInstanceId()));
                        if (parent != null)
                            entries.add(Map.entry("objectId", parent.getId()));
                        entries.add(Map.entry("seq", seqNext + "," + path(attribute) + "]"));
                        entries.add(Map.entry("instanceId", link.getInstanceId()));
                    });
                }).collect(Collectors.toList()), entries -> {
                    entries.add(Map.entry("seq", seqNext));
                    entries.add(Map.entry("instanceId", link.getInstanceId()));
                });
            default:
                return null;
        }
    }

    protected String path(MetaPrototype metaPrototype) {
        return JSON.toJSONString(List.of(metaPrototype.getTypeId(), metaPrototype.getId()));
    }

    protected Map<String, Object> toTreeNode(MetaPrototype obj, List<Map<String, Object>> list) {
        return toTreeNode(obj, list, null);
    }

    protected Map<String, Object> toTreeNode(MetaPrototype obj, List<Map<String, Object>> list, MessagePassingQueue.Consumer<List<Map.Entry<String, Object>>> consumer) {
        List<Map.Entry<String, Object>> list1 = new LinkedList<>();
        list1.add(Map.entry("id", obj.getId()));
        list1.add(Map.entry("code", obj.getCode()));
        if (obj instanceof ITypeEntity) {
            list1.add(Map.entry("type", ((ITypeEntity<?>) obj).getType().getDesc()));
            list1.add(Map.entry("typeId", obj.getTypeId()));
        }
        if (list != null)
            list1.add(Map.entry("children", list));
        else
            list1.add(Map.entry("leaf", true));

        if (consumer != null) {
            consumer.accept(list1);
        }

        return Map.ofEntries(list1.toArray(new Map.Entry[0]));
    }

    /*@VertxRouter(path = "\\/attr\\/bind\\/obj\\/(?<id>\\d+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void attrBindObj(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            String path = (String) JsonUtils.getKeyValue(JSONObject.parseObject(routingContext.body().asString()), "path");
            LinkedList<MetaPrototype> list = getAttrPath(path, MetaUtils.getInstance().getObject(Long.parseLong(routingContext.pathParam("id"))));
            Assert.isTrue(list.peekLast() instanceof MetaObject.Attribute*//*notNull(attribute*//*, () -> String.format("没有找到对应字段:%s", path));
//            logger.info("{},{},{}", object, table, attribute);
            logger.info("{},{}", path, list.stream().map(Objects::toString).collect(Collectors.joining(",")));
            routingContext.end(JSON.toJSONString("{}"));
        });
    }*/

    protected LinkedList<MetaPrototype> getAttrPath(String path, MetaObject metaObject) {
        LinkedList<MetaPrototype> list = new LinkedList<>();
//        list.add( metaObject);
        JSONArray array = JSON.parseArray(path);
        for (int i = 0; i < array.size(); i++) {
            JSONArray s = array.getJSONArray(i);
            IMetaType iMetaType = MetaUtils.getInstance().getMetaType(s.getLong(0));
            if (iMetaType instanceof AttributeMetaType) {
                long attrId = s.getLong(1);
                list.add(((MetaLink) list.peekLast()).getObject().getAttributes().stream().filter(attribute1 -> Objects.equals(attribute1.getId(), attrId)).findFirst().orElseThrow(() -> new IllegalStateException(String.format("表:[%s]没有找到对应字段:%s", path))));
                break;
            }
            if (iMetaType instanceof LinkMetaType) {
                long linkId = s.getLong(1);
                for (MetaLink constructorLink : ((DataObject) (list.peekLast() == null ? metaObject : ((MetaLink) list.peekLast()).getObject())).getConstructorLinks()) {
                    if (Objects.equals(constructorLink.getId(), linkId)) {
                        list.add(constructorLink);
                    }
                }
                continue;
            }
            throw new IllegalStateException(String.format("错误的路径:%s,分片：%s", path, s));
        }
        return list;
    }

    @VertxRouter(path = "\\/attr\\/mapper\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void attrMapper(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            MetaObject.Attribute attribute = MetaUtils.getInstance().getAttribute(Long.parseLong(routingContext.pathParam("id")));
            List<Map<String, Object>> cols = new LinkedList<>();
            List<List<Map<String, Object>>> rows = new LinkedList<>();
            if (attribute instanceof DataObject.Field) {
                MetaLink foreignKey = ((DataObject.Field) attribute).getForeignKey();
                List<Map.Entry<String, Object>> list = new LinkedList<>();
                for (MetaLink child : foreignKey.getChildren()) {
                    MetaLink childChild;
                    for (int i = 0; i < child.getChildren().size(); i++) {
                        childChild = child.getChildren().get(i);
                        list.clear();
                        this.addEntry(list, "id", childChild.getId());
                        this.addEntry(list, "attributeId", childChild.getAttributeId());
                        this.addEntry(list, "objectId", childChild.getObjectId());
                        this.addEntry(list, "instanceId", childChild.getInstanceId());
                        switch (childChild.getType()) {
                            case Head:
                                /*cols.add(Map.of("id", childChild.getId(),
                                        "attributeId", childChild.getId(),
                                        "objectId", childChild.getObjectId(),
                                        "instanceId", childChild.getInstanceId()));*/
                                cols.add(Map.ofEntries(list.toArray(new Map.Entry[0])));
//                                rows.add(new LinkedList<>());
                                break;
                            case Cell:
                                if (rows.size() < i)
                                    rows.add(new LinkedList<>());
                                rows.get(i - 1).add(Map.ofEntries(list.toArray(new Map.Entry[0])));
                                break;
                        }
                    }
                }
            }
            routingContext.end(JSON.toJSONString(Map.of("cols", cols, "rows", rows)));
        });
    }

    protected <K, V> void addEntry(List<Map.Entry<K, V>> list, K key, V value) {
        if (key != null && value != null)
            list.add(Map.entry(key, value));
    }

    @VertxRouter(path = "\\/attr\\/path\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void attrPath(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            MetaObject.Attribute attribute = MetaUtils.getInstance().getAttribute(Long.parseLong(routingContext.pathParam("id")));
            List<MetaLink> list = new LinkedList<>();
            if (attribute instanceof DataObject.Field) {
                /*MetaLink next = attribute.getParent().getChildren().stream().filter(child -> child.getType() == LinkMetaType.SqlToSelect).findFirst().orElse(null);
                while (next != null) {
                    if (next.getLinkId() < 0) {
                        list.add(new MetaLink().<MetaLink>setId(next.getLinkId()).setAttributeId(next.getAttributeId()).setTypeId(LinkMetaType.ObjConstructor.getId()));
                    } else {
                        list.add(next.getLink());
                    }
                    next = next.getChildren().stream().filter(child -> child.getType() == LinkMetaType.SqlToSelect).findFirst().orElse(null);
                }*/
                list.addAll(((DataObject.Field) attribute).columnMapper());
            }
            ListIterator<MetaLink> listIterator = list.listIterator();
            MetaLink metaLink;
            List<String> list1 = new LinkedList<>();
            while (listIterator.hasNext()) {
                metaLink = listIterator.next();
                list1.add(path(metaLink));
                if (!listIterator.hasNext()) {
                    list1.add(path(metaLink.getAttribute()));
                }
            }
            routingContext.end(String.format("[%s]", list1.stream().collect(Collectors.joining(","))));
        });
    }

    @VertxRouter(path = "\\/obj\\/template\\/(?<type>\\w+)\\/(?<id>\\d+)",
            method = {"POST", "GET"},
            mode = VertxRouteType.PathRegex)
    public void template(RoutingContextChain chain) {
        chain.blockingHandler(event -> {
            long qid = Long.parseLong(event.pathParam("id"));
            MetaObject metaObject = MetaUtils.getInstance().getObject(qid);
            switch (event.pathParam("type")) {
                case "query":
                    logger.info(event.body().asString());
                    List<Object> data = new LinkedList<>();
                    for (int i = 0; i < 21; i++) {
                        data.add(Map.of());
                    }
                    QueryTemplateInstance parameter = JSON.parseObject(event.body().asString(), QueryTemplateInstance.class);
                    vertxSqlDataBasePool.page("query", parameter, (objects, throwable) -> {
                        Optional.ofNullable(throwable)
                                .ifPresentOrElse(event::fail, () -> event.end(JSON.toJSONString(objects)));
                    });
//                    event.end(JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect));
                    break;
                case "download":
                    TemplateFormatContent templateFormatContent = new XmlTemplateFormatContent();
                    new org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent(MetaUtils.getInstance().getObject(qid)).format(templateFormatContent);
                    templateFormatContent.build();
                    break;
                case "snapshot":
                    QueryTemplateInstance parameter1 = new QueryTemplateInstance(metaObject);
                    event.end(JSON.toJSONString(parameter1, SerializerFeature.DisableCircularReferenceDetect));
                    break;
                default:
                    event.response().setStatusCode(500).end("error");
                    break;
            }

        });
    }

    @VertxRouter(path = "\\/link\\/(?<id>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void delLink(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            long linkId = Long.parseLong(routingContext.pathParam("id"));
            MetaLink metaLink = MetaUtils.getInstance().getMetaLinkDao().get(linkId);
            metaLink.remove();
            routingContext.end("删除成功");
        });
    }

}
