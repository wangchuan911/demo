package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 16:47
 */
public class SqlContent {
    final static Logger logger = LoggerFactory.getLogger(SqlContent.class);
    protected List<List<MetaLink>> links = new LinkedList<>();
    protected volatile static Class<? extends SqlContent> type;
    protected int scope = 0;


    public void addLink(MetaLink link) {
        if (link.getType() == LinkMetaType.SqlToJoin) {
            if (scope == 0 && links.isEmpty()) {
                links.add(new LinkedList<>());
            } else {
                scope++;
                links.add(new LinkedList<>());
            }
        }
        links.get(scope).add(link);
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

    protected String toSqlJoin(String joinOpr, String condOpr, MetaLink metaLink) {
        return String.format(" %s %s %s %s %s", joinOpr, metaLink.getObject().getCode(), this.toTableAlias(metaLink), condOpr, metaLink.getChildren().stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this);
        }).collect(Collectors.joining(" and ")));
    }


    protected void toSqlJoin(StringBuilder fromBlock, StringBuilder whereBlock, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfStrongRel:
                list.stream().forEach(metaLink -> {
                    fromBlock.append(toSqlJoin("join", "on", metaLink));
                });
                break;
            case SqlToJoinOfWeakRel:
                list.stream().forEach(metaLink -> {
                    fromBlock.append(toSqlJoin("left join", "on", metaLink));
                });
                break;
            case SqlToJoinOfMultiDataRel:
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
                whereBlock.append(" exist (select 1 ").append(_fromBlock).append(_whereBlock).append(")");
                break;
        }
    }

    public String toSqlJoin() {
        StringBuilder fromBlock = new StringBuilder();
        StringBuilder whereBlock = new StringBuilder();
        Optional<MetaLink> optional = links.stream().findFirst().map(list -> list.stream().filter(metaLink -> metaLink.getType() == LinkMetaType.SqlToJoin).findFirst()).orElse(Optional.ofNullable(null));
        Assert.isTrue(optional.isPresent(), "缺少主表");
        MetaLink mainTable = optional.get();
        links.get(0).remove(optional.get());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<LinkMetaType> linkMetaTypes = ApplicationContextProvider.getBean(SqlBuilderHandler.class).linkMetaTypes;
        links.stream().forEach(list -> {
            scope = atomicInteger.getAndIncrement();
            list.stream().collect(Collectors.groupingBy(MetaLink::getType)).forEach((linkMetaType, list1) -> {
                if(linkMetaTypes.stream().noneMatch(linkMetaType1 -> linkMetaType.isMatched(linkMetaType1, Side.Up))){
                    return;
                }
                toSqlJoin(fromBlock, whereBlock, linkMetaType, list1);
            });
        });

        return String.format("select %s from %s %s %s %s",
                Arrays.stream(mainTable.getObject().getAttributes())
                        .map(attribute -> toTableAlias(mainTable) + "." + attribute.getCode())
                        .collect(Collectors.joining(",")),
                mainTable.getObject().getCode(),
                toTableAlias(mainTable),
                fromBlock,
                getWhereBody(whereBlock, mainTable.getChildren()));
    }

    protected String getWhereBody(StringBuilder whereBlock, List<MetaLink> mainTableWhere) {
        StringBuilder builder = new StringBuilder();
        builder.append(mainTableWhere.stream().map(child -> {
            return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this);
        }).collect(Collectors.joining(" and ")));
        if (builder.length() > 0) {
            builder.append(" and ");
        }
        builder.append(whereBlock);
        if (builder.length() > 0) {
            builder.insert(0, " where ");
        }
        return builder.toString();
    }

    public String toTableAlias(MetaLink metaLink) {
        if (scope < 26) {
            return String.format("%s%d", (char) ('A' + scope), metaLink.getInstanceId());
        } else {
            int v = scope;
            StringBuilder stringBuilder = new StringBuilder();
            do {
                stringBuilder.insert(0, (char) ('A' + (v % 26)));
                v /= 26;
            } while (v > 0);
            return String.format("%s%d", (char) ('A' + v), metaLink.getInstanceId());
        }
    }
}
