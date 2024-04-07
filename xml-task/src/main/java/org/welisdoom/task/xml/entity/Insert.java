package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Insert
 * @Description TODO
 * @Author Septem
 * @Date 18:01
 */
@Tag(value = "insert", parentTagTypes = Executable.class, desc = "sql写入")
@Attr(name = "id", desc = "唯一标识")
public class Insert extends Unit implements Script, Copyable {
    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println(getScript(data.getBus(), " "));
        data.next(null);
    }*/
    Boolean isStaticContent;
    DataBaseConnectPool.StaticSql sql;

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        if (isStaticContent == null) {
            isStaticContent = children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).isStaticContent()).reduce(Boolean.TRUE, (aBoolean, aBoolean2) -> aBoolean && aBoolean2);
        }
//        System.out.println(data.getBus());
        return Database.doConnect(this, data, connection -> {
//            System.out.println(data.getBus());
            DataBaseConnectPool pool = Database.getDataBase(Insert.this);
            if (!isStaticContent) {
                String sql = getScript(data);
                List<Object> params = new LinkedList<>();
                pool.setValueToSql(params, pool.getSqlParamTypes(sql), data.getOgnlContext(), data.getBus());
                sql = pool.sqlFormat(sql, params);
                Tuple tuple = Tuple.tuple(params);
                pool.log("sql", sql);
                pool.log("params", tuple);
                return pool.execute(connection, sql, tuple);
            } else {

                List<Object> params = new LinkedList<>();
                if (this.sql == null) {
                    String sql = getScript(data);
                    List<Map.Entry<String, JdbcType>> list = pool.getSqlParamTypes(sql);
                    pool.setValueToSql(params, list, data.getOgnlContext(), data.getBus());
                    sql = pool.sqlFormat(sql, params);
                    this.sql = new DataBaseConnectPool.StaticSql(sql, list);
                    pool.log("static sql", this.sql.getSql());
                } else {
                    pool.setValueToSql(params, this.sql.getTypes(), data.getOgnlContext(), data.getBus());
                }
                Tuple tuple = Tuple.tuple(params);
                pool.log("params", tuple);
                return connection.preparedQuery(this.sql.getSql()).execute(tuple);
            }
        }).compose(rows -> Future.succeededFuture(((RowSet<Row>) rows).rowCount()));
    }

    @Override
    public String getScript(TaskInstance request, String split) {
        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split)).trim();
    }

    public String getScript(TaskInstance request) {
        return getScript(request, " ");
    }


    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }


}
