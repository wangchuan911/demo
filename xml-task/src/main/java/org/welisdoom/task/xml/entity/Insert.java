package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.data.BaseCondition;

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

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        Database.findConnect(this, data).onSuccess(connection -> {
            BaseCondition<String, TaskRequest> condition = new BaseCondition<String, TaskRequest>() {
            };
            condition.setData(data);
            condition.setCondition(data.getBus());
            ((Future<Integer>) Database.getDataBase(Insert.this, data).update(connection, getScript(data), condition)).onSuccess(toNext::complete).onFailure(toNext::fail);
        }).onFailure(toNext::fail);
    }

    public String getScript(TaskRequest request) {
        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, " ")).collect(Collectors.joining(" ")).trim();
    }


    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }
}
