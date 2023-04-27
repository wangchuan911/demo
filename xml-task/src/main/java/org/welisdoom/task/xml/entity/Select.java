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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class}, desc = "sql查询")
@Attr(name = "id", desc = "唯一标识")
public class Select extends Unit implements Executable, Iterable<Map<String, Object>> {

    /*public void execute(TaskRequest data) {
        data.generateData(this);
        String sql = getScript(data.getBus(), " ");
        System.out.println(sql);
        Iterate iterate = getChild(Iterate.class).get(0);
        Promise<Object> parentPromise = data.promise;

        if (data.isDebugger) {
            List<Map<String, Object>> list = List.of(Map.of("test1", "test1", "test2", "test2", "test3", "test3", "test4", "test4"));
            Future<Object> listFuture = Future.succeededFuture();
            for (Map<String, Object> item : list) {
                listFuture = listFuture.compose(o ->
                        Future.future(promise -> {
                            try {
                                data.setPromise(promise);
                                iterate.execute(data, item);
                            } catch (Throwable e) {
                                promise.fail(e);
                                return;
                            }
                        })
                );
            }
            listFuture.onSuccess(parentPromise::complete).onFailure(parentPromise::fail);
        } else {
            SqlConnection transaction = Transactional.getSqlConnection(data, attributes.get("db-name"));
            BaseCondition<Long, TaskRequest> condition = new BaseCondition<Long, TaskRequest>() {
            };
            condition.setPage(new BaseCondition.Page(1, 100));
            condition.setCondition(data.getBus());
            ((Future<RowSet<Row>>) Transactional
                    .getDataBaseConnectPool(transaction)
                    .page(transaction, sql, condition)).onSuccess(result -> {
                Map<String, Object> item = new HashMap<>();
                Future<Object> listFuture = Future.succeededFuture();
                for (Row row : result) {
                    for (int i = 0; i < row.size(); i++) {
                        item.put(row.getColumnName(i), row.getValue(row.getColumnName(i)));
                    }
                    listFuture = listFuture.compose(o ->
                            Future.future(promise -> {
                                try {
                                    data.setPromise(promise);
                                    iterate.execute(data, item);
                                } catch (Throwable e) {
                                    promise.fail(e);
                                    return;
                                }
                            })
                    );
                }
                listFuture.onSuccess(parentPromise::complete).onFailure(parentPromise::fail);
            });
        }

    }*/

    protected void debugStart(TaskRequest data, Promise<Object> toNext) {
        List<Map<String, Object>> list = List.of(Map.of("test1", "test1", "test2", "test2", "test3", "test3", "test4", "test4"));
        Future<Object> listFuture = Future.succeededFuture();
        for (Map<String, Object> item : list) {
            listFuture = listFuture.compose(o ->
                    startChildUnit(data, item, typeMatched(Iterator.class))
            );
        }
        listFuture.onSuccess(toNext::complete).onFailure(toNext::fail);
    }

    /*protected DataBaseConnectPool getDataBase(TaskRequest data) {
        return Database.getDatabase(data, attributes.get("link"));
    }

    protected Future<SqlConnection> findConnect(TaskRequest data) {
        Unit p = this.parent;
        while (!(p == null || p instanceof Transactional)) {
            p = p.parent;
        }
        if (p != null)
            return Future.succeededFuture(((Transactional) p).getSqlConnection(data));
        return getDataBase(data).getConnect(attributes.get("link"));
    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        data.generateData(this);
        String sql = getScript(data);

        if (data.isDebugger) {
            debugStart(data, toNext);
        } else {

            Database.findConnect(this, data).onSuccess(connection -> {
                BaseCondition<String, TaskRequest> condition = new BaseCondition<>() {
                    {
                        setData(data);
                        setPage(new BaseCondition.Page(1, 100));
                        setCondition(data.getBus());
                    }
                };
                AtomicLong index = new AtomicLong(0);
                Future<Object> future = Database
                        .getDataBase(this, data)
                        .pageScroll(connection, sql, condition, rows -> {
                            Future<Object> listFuture = Future.succeededFuture();
                            for (Row row : (RowSet<Row>) rows) {
                                /*listFuture = listFuture.compose(o ->
                                        this.iterator(data, Item.of(index.incrementAndGet(), this.rowToMap(row)))
                                );*/
                                listFuture = bigFutureLoop(this.rowToMap(row), Iterable.countReset(index, 500, 0), 500, listFuture, data);
                            }
                            return listFuture;
                        });
                future.onSuccess(o -> {
                    toNext.complete(index.get());
                }).onFailure(throwable -> {
                    Break.onBreak(throwable, toNext, index.get());
                });

            }).onFailure(toNext::fail);
        }
    }

    Map<String, Object> rowToMap(Row row) {
        List<Map.Entry> entries = new LinkedList<>();
        for (int i = 0; i < row.size(); i++) {
            if (row.getColumnName(i).equals("@RowNum")) continue;
            entries.add(Map.entry(row.getColumnName(i), row.getValue(i)));
        }
        return Map.ofEntries(entries.toArray(Map.Entry[]::new));
    }

    protected String getScript(TaskRequest data) {
        return getChild(Sql.class).get(0).getScript(data, " ").trim();
    }

    public static void main(String[] args) {
        Matcher matcher = DataBaseConnectPool.PATTERN.matcher("${asd,jdbcType=asd},${asdaaa,jdbcType=asd}==============${asddasd,jdbcType=asd}");
        while (matcher.find()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            ;
        }
    }
}
