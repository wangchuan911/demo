package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.data.BaseCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @Classname Insert
 * @Description TODO
 * @Author Septem
 * @Date 18:01
 */
@Tag(value = "insert", parentTagTypes = Executable.class, desc = "sql写入")
@Attr(name = "id", desc = "唯一标识")
public class Insert extends Unit implements Executable, Copyable {
    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println(getScript(data.getBus(), " "));
        data.next(null);
    }*/
    Boolean isStaticContent;
    DataBaseConnectPool.StaticSql sql;

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        if (isStaticContent == null) {
            isStaticContent = children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).isStaticContent()).reduce(Boolean.TRUE, (aBoolean, aBoolean2) -> aBoolean && aBoolean2);
        }
//        System.out.println(data.getBus());
        Future<Object> future = Database.findConnect(this, data).compose(connection -> {
            BaseCondition<String, TaskRequest> condition = new BaseCondition<String, TaskRequest>() {
            };
            condition.setData(data);
            condition.setCondition(data.getBus());
//            System.out.println(data.getBus());
            DataBaseConnectPool pool = Database.getDataBase(Insert.this, data);
            if (!isStaticContent) {
                return pool.update(connection, getScript(data), condition);
            } else {

                List<Object> params = new LinkedList<>();
                if (this.sql == null) {
                    String sql = getScript(data);
                    List<Map.Entry<String, JdbcType>> list = pool.getSqlParamTypes(sql);
                    pool.setValueToSql(params, list, condition);
                    sql = pool.sqlFormat(sql, params);
                    this.sql = new DataBaseConnectPool.StaticSql(sql, list);
                    pool.log("static sql", this.sql.getSql());
                } else {
                    pool.setValueToSql(params, this.sql.getTypes(), condition);
                }
                Tuple tuple = Tuple.tuple(params);
                pool.log("params", tuple);
                return connection.preparedQuery(this.sql.getSql()).execute(tuple).compose(rows -> Future.succeededFuture(rows.rowCount()));
            }
        });
        future.onSuccess(toNext::complete).onFailure(toNext::fail);
    }

    public String getScript(TaskRequest request) {
        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, " ")).collect(Collectors.joining(" ")).trim();
    }


    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }


}
