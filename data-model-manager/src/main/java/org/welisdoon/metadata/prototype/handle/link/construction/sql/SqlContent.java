package org.welisdoon.metadata.prototype.handle.link.construction.sql;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.Side;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 16:47
 */
public class SqlContent {
    final static Logger logger = LoggerFactory.getLogger(SqlContent.class);
    protected List<MetaLink> links = new LinkedList<>();
    protected volatile static Class<? extends SqlContent> type;
    protected SqlContent parent;
    protected volatile boolean locked = false;
    List<LinkMetaType> linkMetaTypes = null;

    public void addLink(MetaLink link) {
        checkState();
        links.add(link);
    }

    public SqlContent() {

    }

    public void setParent(SqlContent parent) {
        this.parent = parent;
    }

    protected void checkState() {
        Assert.isTrue(!locked, "content is locked");
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


    protected synchronized List<LinkMetaType> getLinkMetaTypes() {
        if (linkMetaTypes == null) {
            if (this.parent != null) {
                linkMetaTypes = this.parent.getLinkMetaTypes();
            }
        }
        if (linkMetaTypes == null) {
            linkMetaTypes = ApplicationContextProvider.getBean(SqlBuilderHandler.class).linkMetaTypes;
        }
        return linkMetaTypes;
    }

    public String toSqlJoin() {
        checkState();
        lock(true);
        try {
            return String.format("select %s %s %s ", columnsToSql().toString(), tablesToSql().toString(), whereToSqlFormat().toString());
        } finally {
            lock(false);
        }
    }

    protected StringBuilder whereToSqlFormat() {
        StringBuilder sql = Optional.ofNullable(whereToSql()).orElseGet(StringBuilder::new);
        while (sql.charAt(0) == ' ') {
            sql.delete(0, 1);
        }
        for (int i = sql.length() - 1; sql.charAt(i) == ' '; i--) {
            if (sql.charAt(i) == ' ') {
                sql.delete(i, i + 1);
                continue;
            }
            break;
        }
        if (sql.length() == 0 || sql.lastIndexOf("and") == 0) {
            sql.setLength(0);
            return sql;
        }
        if (sql.lastIndexOf(" and") == sql.length() - 4) {
            sql = sql.delete(sql.length() - 4, 4);
        }
        sql.insert(0, " where ");
        return sql;
    }

    protected void lock(boolean locked) {
        this.locked = locked;
    }

    protected StringBuilder tablesToSql() {
        StringBuilder sql = this.parent == null ? new StringBuilder() : parent.tablesToSql();
        List<LinkMetaType> linkMetaTypes = getLinkMetaTypes();
        Stream<MetaLink> stream = links.stream();
        if (this.parent == null) {
            sql.append(" from ");
            MetaLink mainTable = links.stream().findFirst().orElseThrow(() -> new IllegalStateException("缺少主表"));
            sql.append(mainTable.getObject().getCode()).append(" ").append(toTableAlias(mainTable));
            stream = stream.skip(1);
        }
        stream.collect(Collectors.groupingBy(MetaLink::getType)).forEach((linkMetaType, list1) -> {
            if (!matchLinkMetaType(linkMetaTypes, linkMetaType)) {
                return;
            }
            tablesToSql(sql, linkMetaType, list1);
        });
        return sql;
    }

    protected void tablesToSql(StringBuilder sql, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
            case SqlToJoinOfStrongRel:
                list.stream().forEach(metaLink -> {
                    sql.append(toSqlJoin("join", "on", metaLink));
                });
                break;
            case SqlToJoinOfWeakRel:
                list.stream().forEach(metaLink -> {
                    sql.append(toSqlJoin("left join", "on", metaLink));
                });
                break;
        }
    }

    protected StringBuilder whereToSql() {
        StringBuilder sql = this.parent == null ? new StringBuilder() : parent.whereToSql();
        List<LinkMetaType> linkMetaTypes = getLinkMetaTypes();
        Stream<MetaLink> stream = links.stream();
        if (this.parent == null) {
            MetaLink mainTable = links.stream().findFirst().orElseThrow(() -> new IllegalStateException("缺少主表"));
            mainTable.getChildren().stream().map(child -> {
                return ISqlBuilderHandler.getHandler(child.getType()).toSql(child, this);
            }).collect(Collectors.joining(" and "));
            stream = stream.skip(1);
            sql.append(" and ");
        }
        links.stream().skip(1).collect(Collectors.groupingBy(MetaLink::getType)).forEach((linkMetaType, list1) -> {
            if (!matchLinkMetaType(linkMetaTypes, linkMetaType)) {
                return;
            }
            whereToSql(sql, linkMetaType, list1);
        });

        return sql;
    }

    protected boolean matchLinkMetaType(List<LinkMetaType> linkMetaTypes, LinkMetaType linkMetaType) {
        return linkMetaTypes.stream().anyMatch(linkMetaType1 -> linkMetaType.isMatched(linkMetaType1, Side.Up));
    }

    protected void whereToSql(StringBuilder sql, LinkMetaType type, List<MetaLink> list) {
        switch (type) {
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
                sql.append(" exist (select 1 ").append(_fromBlock).append(_whereBlock).append(")");
                break;
        }
    }

    protected StringBuilder columnsToSql() {
        return new StringBuilder("*");
    }

    public String toTableAlias(MetaLink metaLink) {
        int scope = 0;
        SqlContent parent = this;
        while (Objects.nonNull((parent = parent.parent))) {
            scope++;
        }
        if (scope < 26) {
            return String.format("%s%d", (char) ('A' + scope), metaLink.getInstanceId());
        } else {
            int v = scope;
            StringBuilder stringBuilder = new StringBuilder();
            do {
                stringBuilder.insert(0, (char) ('A' + (v % 26)));
                v /= 26;
            } while (v > 0);
            return String.format("%s%d", stringBuilder.toString(), metaLink.getInstanceId());
        }
    }
}
