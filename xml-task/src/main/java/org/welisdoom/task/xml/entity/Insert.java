package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Insert
 * @Description TODO
 * @Author Septem
 * @Date 18:01
 */
@Tag(value = "insert", parentTagTypes = Executable.class)
public class Insert extends Unit implements Script {
    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println(getScript(data.getBus(), " "));
        data.next(null);
    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        log(getScript(data.getBus(), " "));
        toNext.complete(null);
    }

    @Override
    public String getScript(Map<String, Object> data, String split) {
        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(data, split)).collect(Collectors.joining(split)).trim();
    }
}
