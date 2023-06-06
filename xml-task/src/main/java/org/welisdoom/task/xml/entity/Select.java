package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.*;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoon.common.data.BaseCondition;
import org.xml.sax.Attributes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class}, desc = "sql查询")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "mode", options = {"page", "tetris"}, defaultOption = 0)
public class Select extends Unit implements Executable, Iterable<Map<String, Object>> {

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        data.generateData(this);

        Database.findConnect(this, data).onSuccess(connection -> {
            BaseCondition.Page page = new BaseCondition.Page(1, 100);
            AtomicLong index = new AtomicLong(0);
            DataBaseConnectPool pool = Database.getDataBase(this);
            String sql = getScript(data);
            List<Object> params = new LinkedList<>();
            pool.setValueToSql(params, pool.getSqlParamTypes(sql = pool.toPageSql(sql)), data.getOgnlContext(), data.getBus());
            sql = pool.sqlFormat(sql, params);
            Future<Object> future = pageScroll(pool, sql, params, data, page, rows -> {
                Future<Object> listFuture = Future.succeededFuture();
                for (Map<String, Object> row : rows) {
                    listFuture = futureLoop(Item.of(index.incrementAndGet(), row), listFuture, data);
                }
                return listFuture;
            });

            future.compose(o -> loopEnd(data)).onComplete(event -> {
                if (event.succeeded()) {
                    toNext.complete(index.get());
                } else {
                    Break.onBreak(event.cause(), toNext, index.get());
                }
            })/*.onSuccess(o -> {
                    toNext.complete(index.get());
                }).onFailure(throwable -> {
                    Break.onBreak(throwable, toNext, index.get());
                })*/;

        }).onFailure(toNext::fail);

    }

    Map<String, Object> rowToMap(Row row) {
        List<Map.Entry> entries = new LinkedList<>();
        for (int i = 0; i < row.size(); i++) {
            if (row.getColumnName(i).equals("@RowNum")) continue;
            if (row.getValue(i) == null)
                continue;
            entries.add(Map.entry(row.getColumnName(i), row.getValue(i)));
        }
        return Map.ofEntries(entries.toArray(Map.Entry[]::new));
    }

    Collection<Map<String, Object>> rowToMaps(RowSet<Row> rows) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (Row row : rows) {
            list.add(rowToMap(row));
        }
        return list;
    }

    protected String getScript(TaskRequest data) {
        return getChild(Sql.class).get(0).getScript(data, " ").trim();
    }


    Future<Object> pageScroll(DataBaseConnectPool pool, String sql, List<Object> list, TaskRequest data, BaseCondition.Page page, Function<Collection<Map<String, Object>>, Future<Object>> future) {
        Tuple tuple = Tuple.tuple(list);
        pool.setPage(tuple, page);
        pool.log("sql", sql);
        pool.log("params", tuple);
        int nextPageNum = page.getPage() + ("tetris".equals(attributes.get("mode")) ? 0 : 1);
        log(String.format("第%d页", nextPageNum));
        return compose(Database.findConnect(this, data), connection ->
                compose(pool.execute(connection, sql, tuple), rows -> {
                    Promise<Object> promise = Promise.promise();
                    Collection<Map<String, Object>> results = rowToMaps((RowSet<Row>) rows);
                    Database.releaseConnect(this, data).onComplete(event -> {
                        future.apply(results).onComplete(event1 -> {
                            complete(event1, promise);
                        });
                    });
                    return compose(promise.future(), o -> results.size() < page.getPageSize() ?
                            Future.succeededFuture() :
                            pageScroll(pool, sql, list, data, page.setPage(nextPageNum), future));
                }));
    }
}
