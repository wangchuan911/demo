package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Transactional
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "transactional", parentTagTypes = Executable.class, desc = "事务")
@Attr(name = "id", desc = "唯一标识")
public class Transactional extends Unit implements Executable {
    Map<TaskRequest, SqlConnection> MAP = new HashMap<>();

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {

        ((Future<Object>) Database.getDatabase(data, attributes.get("link")).getConnect(attributes.get("link"))).onSuccess(o -> {
            MAP.put(data, (SqlConnection) o);
            super.start(data, toNext);
        }).onFailure(toNext::fail);
    }


    public SqlConnection getSqlConnection(TaskRequest request) {
        return MAP.get(request);
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        MAP.remove(taskRequest);
    }
}
