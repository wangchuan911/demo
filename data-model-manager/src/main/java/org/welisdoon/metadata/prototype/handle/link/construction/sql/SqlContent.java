package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 16:47
 */
public class SqlContent {
    final static Logger logger = LoggerFactory.getLogger(SqlContent.class);
    protected List<MetaLink> selects = new LinkedList<>();
    protected List<MetaLink> joins = new LinkedList<>();
    protected volatile static Class<? extends SqlContent> type;

    public void setJoins(List<MetaLink> joins) {
        this.joins = joins;
    }

    public List<MetaLink> getJoins() {
        return joins;
    }

    public void setSelects(List<MetaLink> selects) {
        this.selects = selects;
    }

    public List<MetaLink> getSelects() {
        return selects;
    }

    public static SqlContent getInstance() {
        Class<? extends SqlContent> type = ObjectUtils.synchronizedGet(SqlContent.class, sqlContentClass -> SqlContent.type, sqlContentClass -> {
            Environment environment = ApplicationContextProvider.getBean(Environment.class);
            try {
                Class<?> aType = Class.forName(Optional.ofNullable(environment.getProperty("metadata.link.sqlContent.type")).orElse(SqlContent.class.getName()));
                Assert.isTrue(SqlContent.class.isAssignableFrom(aType), String.format("type：%s must be extend org.welisdoon.metadata.prototype.handle.link.construction.sql.SqlContent", SqlContent.type));
                SqlContent.type = (Class<? extends SqlContent>) aType;
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
                SqlContent.type = SqlContent.class;
            }
            return SqlContent.type;
        });
        return JSONObject.parseObject("{}", type);
    }

    protected String toSql(String joinOpr, String condOpr, MetaLink metaLink) {
        return String.format(" %s %s %s %s %s", joinOpr, metaLink.getObject().getCode(), this.toTableAlias(metaLink), condOpr, metaLink.getChildren().stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this);
        }).collect(Collectors.joining(" and ")));
    }


    protected void toSql(StringBuilder fromBlock, StringBuilder whereBlock, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToFromOfStrongRel:
                list.stream().forEach(metaLink -> {
                    fromBlock.append(toSql("join", "on", metaLink));
                });
                break;
            case SqlToFromOfWeakRel:
                list.stream().forEach(metaLink -> {
                    fromBlock.append(toSql("left join", "on", metaLink));
                });
                break;
            case SqlToFromOfMultiDataRel:
                StringBuilder _fromBlock = new StringBuilder();
                StringBuilder _whereBlock = new StringBuilder();
                list.stream().forEach(metaLink -> {
                    StringBuilder _sql, _sql2;
                    String linkTableOpr, LinkCondOpr;
                    if (_fromBlock.length() == 0) {
                        _sql = _fromBlock;
                        _sql2 = _whereBlock;
                        linkTableOpr = " from ";
                        LinkCondOpr = " where ";
                    } else {
                        _sql = _fromBlock;
                        _sql2 = _fromBlock;
                        linkTableOpr = " join ";
                        LinkCondOpr = " on ";
                    }
                    _sql.append(linkTableOpr).append(metaLink.getObject().getCode()).append(toTableAlias(metaLink));
                    _sql2.append(LinkCondOpr);
                    metaLink.getChildren().forEach(child -> {
                        _sql2.append(ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this));
                    });
                });
                whereBlock.append(" and exist (select 1 ").append(_fromBlock).append(_whereBlock).append(")");
                break;
        }
    }

    public String toSql() {
        StringBuilder fromBlock = new StringBuilder();
        StringBuilder whereBlock = new StringBuilder();

        Map<LinkMetaType, List<MetaLink>> map = joins.stream().collect(Collectors.groupingBy(MetaLink::getType));
        Assert.isTrue(map.getOrDefault(LinkMetaType.SqlToFrom, Collections.emptyList()).size() > 0, "缺少主表");
        MetaLink mainTable = map.get(LinkMetaType.SqlToFrom).get(0);


        map.entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), LinkMetaType.SqlToFrom)).forEach(entry -> {
            toSql(fromBlock, whereBlock, entry.getKey(), map.get(entry.getKey()));
        });

        return new StringBuilder().append("select ")
                .append(Arrays
                        .stream(mainTable.getObject().getAttributes())
                        .map(attribute -> toTableAlias(mainTable) + "." + attribute.getCode())
                        .collect(Collectors.joining(",")))
                .append(" from ").append(mainTable.getObject().getCode()).append(toTableAlias(mainTable))
                .append(fromBlock)
                .append(" where ")
                .append(mainTable.getChildren().stream().map(child -> {
                    return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this);
                }).collect(Collectors.joining(" and ")))
                .append(whereBlock).toString();
    }

    public static String toTableAlias(MetaLink metaLink) {
        return "T" + metaLink.getInstanceId();
    }
}