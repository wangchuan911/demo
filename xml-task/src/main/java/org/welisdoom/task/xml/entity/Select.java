package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.data.BaseCondition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname Select
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@Tag(value = "select", parentTagTypes = {Executable.class})
public class Select extends Unit implements Script {

    public Future<Object> execute(TaskRequest data) {
        data.generateData(this);
        String sql = getScript(data.getBus(), " ");
        System.out.println(sql);
        Iterate iterate = getChild(Iterate.class).get(0);
        return Future.future(promise -> {
            if (data.isDebugger) {
                List<Map<String, Object>> list = List.of(Map.of("test1", "test1", "test2", "test2", "test3", "test3", "test4", "test4"));
                for (Map<String, Object> item : list) {
                    try {
                        iterate.execute(data, item);
                    } catch (Throwable e) {
                        promise.fail(e);
                        return;
                    }
                }
                return;
            }
            SqlConnection transaction = Transactional.getSqlConnection(data, attributes.get("db-name"));
            BaseCondition<Long, TaskRequest> condition = new BaseCondition<Long, TaskRequest>() {
            };
            condition.setPage(new BaseCondition.Page(1, 100));
            condition.setCondition(data.getBus());
            ((Future<RowSet<Row>>) Transactional
                    .getDataBaseConnectPool(transaction)
                    .page(transaction, sql, condition)).onSuccess(o -> {
                Map<String, Object> item = new HashMap<>();
                for (Row row : o) {
                    for (int i = 0; i < row.size(); i++) {
                        item.put(row.getColumnName(i), row.getValue(row.getColumnName(i)));
                    }
                    try {
                        iterate.execute(data, item);
                    } catch (Throwable e) {
                        promise.fail(e);
                        return;
                    }
                }
            }).onFailure(promise::fail);
            promise.complete();
        });
    }

    @Override
    public String getScript(Map<String, Object> data, String s) {
        return getChild(Sql.class).get(0).getScript(data, s).trim();
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
