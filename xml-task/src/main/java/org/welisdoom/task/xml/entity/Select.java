package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.sync.DatasouceConnectManager;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoon.common.data.BaseCondition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class}, desc = "sql查询")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "mode", options = {"page", "tetris"}, defaultOption = 0)
@Attr(name = "size", type = Integer.class)
public class Select extends Unit implements Executable, Iterable<Map<String, Object>> {

    @Override
    protected void startSync(TaskSession data) throws Throwable {
//        data.generateData(this);
        BaseCondition.Page page = new BaseCondition.Page(1, Math.max(MapUtils.getInteger(attributes, "size", 100), 100));
        AtomicLong index = new AtomicLong(0);
        Database.DataSourceConnect connect = Database.getDataBaseSync(this);
        Database.DataSourceTemplate template = connect.getTemplate();
        DatasouceConnectManager connectPool = connect.getDatasouceConnectPool();
        String sql = connectPool.toPageSql(getScript(data));
        PreparedStatement preparedStatement = template.prepare(sql, data);
        List<Map<String, Object>> list = new LinkedList<>();
        List<SubQuery> queryList = getChild(SubQuery.class);
        try {
            do {
                list.clear();
                connectPool.log("", template.sqlTemplate.log1.toString());
                connectPool.log("", template.sqlTemplate.log2.toString() + "," + connectPool.setPage(preparedStatement, page));
                try (ResultSet row = preparedStatement.executeQuery()) {
                    toMap(row, list);
                }
                if (!("tetris".equals(attributes.get("mode")))) page.nextPage();
                for (Map<String, Object> row1 : list) {
                    for (SubQuery subQuery : queryList) {
                        data.setValue(row1);
                        subQuery.startSync(data);
                    }
                    try {
                        this.iteratorSync(data, Item.of(index.incrementAndGet(), row1));
                    } catch (Break.SkipOneLoopThrowable e) {
                        continue;
                    }
                }
            } while (!list.isEmpty());
        } catch (Break.BreakLoopThrowable e) {
            Break.onBreak(e);
            log(e.getMessage());
        }
        this.await(data);
        connect.close();
    }

    protected void toMap(ResultSet row, List<Map<String, Object>> list) throws SQLException {
        ResultSetMetaData metaData = row.getMetaData();
        Map<String, Object> map;
        while (row.next()) {
            map = new HashMap<>(metaData.getColumnCount(), 1.0F);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (row.getObject(i) == null) continue;
                map.put(metaData.getColumnName(i), row.getObject(i));
            }
            list.add(map);
        }
    }

    @Override
    protected Future<Object> start(TaskSession data, Object preUnitResult) {
        data.generateData(this);
        BaseCondition.Page page = new BaseCondition.Page(1, 100);
        AtomicLong index = new AtomicLong(0);
        DataBaseConnectPool pool = Database.getDataBase(this);
        String sql = getScript(data);
        List<Object> params = new LinkedList<>();
        pool.setValueToSql(params, pool.getSqlParamTypes(sql = pool.toPageSql(sql)), data.getOgnlContext(), data.getBus());
        sql = pool.sqlFormat(sql, params);
        return pageScroll(
                pool, sql, params, data, page,
                rows -> {
                    Future<Object> listFuture = Future.succeededFuture();
                    for (Map<String, Object> row : rows) {
                        listFuture = futureLoop(Item.of(index.incrementAndGet(), row), listFuture, data);
                    }
                    return listFuture;
                }
        )
                .compose(o -> loopEnd(data))
                .compose(event -> Future.succeededFuture(index.get()),
                        throwable -> Break.onBreak(throwable, index.get()));


    }

    List<Map.Entry> rowToEntry(Row row) {
        List<Map.Entry> entries = new LinkedList<>();
        for (int i = 0; i < row.size(); i++) {
            if (row.getColumnName(i).equals("@RowNum")) continue;
            if (row.getValue(i) == null)
                continue;
            entries.add(Map.entry(row.getColumnName(i), row.getValue(i)));
        }
        return entries;
    }

    Map<String, Object> rowToMap(Row row) {
        return entryToMap(rowToEntry(row));
    }

    Map<String, Object> entryToMap(List<Map.Entry> list) {
        return Map.ofEntries(list.toArray(Map.Entry[]::new));
    }

    Collection<Map<String, Object>> rowToMaps(RowSet<Row> rows) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (Row row : rows) {
            list.add(rowToMap(row));
        }
        return list;
    }

    protected String getScript(TaskSession data) {
        return getChild(Sql.class).get(0).getScript(data, " ").trim();
    }


    @Deprecated
    Future<Object> pageScroll(DataBaseConnectPool pool, String sql, List<Object> list, TaskSession data, BaseCondition.Page page, Function<Collection<Map<String, Object>>, Future<Object>> future) {
       /* Tuple tuple = Tuple.tuple(list);
        pool.setPage(tuple, page);
        pool.log("sql", sql);
        pool.log("params", tuple);
        int nextPageNum = page.getPage() + ("tetris".equals(attributes.get("mode")) ? 0 : 1);
        log(String.format("第%d页", nextPageNum));
        return Database.doConnect(this, data, connection -> {
            return ((Future<RowSet<Row>>) pool.execute(connection, sql, tuple)).compose(rows -> {
                return Future.succeededFuture(rowToMaps(rows));
            }).compose(rows -> {
                return future.apply(rows).compose(o -> {
                    return rows.size() < page.getPageSize() ? Future.succeededFuture() : pageScroll(pool, sql, list, data, page.setPage(nextPageNum), future);
                });
            });
        });*/
        int nextPageNum = page.getPage() + ("tetris".equals(attributes.get("mode")) ? 0 : 1);
        log(String.format("第%d页", nextPageNum));
        return page(pool, sql, Tuple.tuple(list), data, page, (rows) -> {
            return future.apply(rows).compose(o -> {
                return rows.size() < page.getPageSize() ? Future.succeededFuture() : pageScroll(pool, sql, list, data, page.setPage(nextPageNum), future);
            });
        });
    }

    Future<Object> page(DataBaseConnectPool pool, String sql, Tuple tuple, TaskSession data, BaseCondition.Page page, Function<Collection<Map<String, Object>>, Future<Object>> result) {
        pool.setPage(tuple, page);
        pool.log("sql", sql);
        pool.log("params", tuple);
        return Database.doConnect(this, data, connection -> ((Future<RowSet<Row>>) pool.execute(connection, sql, tuple)).compose(rows -> subQuery(data, rows)).compose(result::apply));
    }

    Future<List<Map<String, Object>>> subQuery(TaskSession data, RowSet<Row> rows) {
        Future<Object> future = Future.succeededFuture();
        List<Map<String, Object>> mapList = new LinkedList<>();
        for (Row row : rows) {
            List<Map.Entry> list = rowToEntry(row);
            future = future.compose(o ->
                    Future.all(
                            getChild(SubQuery.class).stream()
                                    .map(columnResultSet ->
                                            startChildUnit(data, entryToMap(list), columnResultSet)
                                                    .onSuccess(event -> {
                                                        list.add(Map.entry(columnResultSet.getId(), event));
                                                    }))
                                    .collect(Collectors.toList())))
                    .compose(compositeFuture -> {
                        mapList.add(entryToMap(list));
                        return Future.succeededFuture();
                    });
        }
        return future.compose(o -> Future.succeededFuture(mapList));
    }

    @Tag(value = "sub-query", parentTagTypes = {Executable.class}, desc = "sql查询")
    @Attr(name = "is-list", desc = "是否返回list")
    @Attr(name = "name", desc = "名称")
    public static class SubQuery extends Select {
        @Override
        protected Future<Object> start(TaskSession data, Object preUnitResult) {
            DataBaseConnectPool pool = Database.getDataBase(this);
            TaskSession data1 = new TaskSession(data.getId() + "-" + this.getParent(Unit.class).getId() + "-sub-query");
            data1.getBus().putAll((Map<String, Object>) preUnitResult);
            String sql = getScript(data);
            List<Object> params = new LinkedList<>();
            pool.setValueToSql(params, pool.getSqlParamTypes(sql), data.getOgnlContext(), data.getBus());
            sql = pool.sqlFormat(sql, params);
            String finalSql = sql;
            return Database.doConnect(this, data, connection -> ((Future<RowSet<Row>>) pool.execute(connection, finalSql, Tuple.tuple(params))).compose(rows -> query(data, rows)));
        }

        Future<Object> query(TaskSession data, RowSet<Row> rows) {
            if (Boolean.valueOf(attributes.getOrDefault("list", "false"))) {
                return (Future) super.subQuery(data, rows);
            }
            return super.subQuery(data, rows).compose(maps -> Future.succeededFuture(maps.stream().findFirst().orElse(Collections.emptyMap())));
        }

        boolean isList() {
            return "true".equalsIgnoreCase(attributes.get("is-list"));
        }

        @Override
        protected void startSync(TaskSession data) throws Throwable {
            Database.DataSourceConnect connect = Database.getDataBaseSync(this);
            DatasouceConnectManager connectPool = connect.getDatasouceConnectPool();
            Database.DataSourceTemplate template = connect.getTemplate();
            String sql = connectPool.toPageSql(getScript(data));
            PreparedStatement preparedStatement = template.prepare(sql, data);
            preparedStatement.executeQuery();
            connectPool.log("", template.sqlTemplate.log1.toString());
            connectPool.log("", template.sqlTemplate.log2.toString());
            List<Map<String, Object>> list = new LinkedList<>();
            try (ResultSet row = preparedStatement.executeQuery()) {
                toMap(row, list);
            }
            ((Map) data.value).put(attributes.get("name"), !isList() ? list.stream().findFirst().orElse(new HashMap<>()) : list);
        }
    }


}
