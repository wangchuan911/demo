package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
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
@Attr(name = "id", desc = "唯一标识")
public class Insert extends Unit implements Executable {
    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println(getScript(data.getBus(), " "));
        data.next(null);
    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        log(getScript(data.getBus()));
        toNext.complete(null);
    }

    public String getScript(Map<String, Object> data) {
        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(data, " ")).collect(Collectors.joining(" ")).trim();
    }
}
