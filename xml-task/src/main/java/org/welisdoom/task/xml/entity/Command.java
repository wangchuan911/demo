package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname Commnd
 * @Description TODO
 * @Author Septem
 * @Date 9:45
 */

@Tag(value = "command", parentTagTypes = Executable.class, desc = "命令", nameSpace = "system")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "db", desc = "数据库类型")
public class Command extends Unit implements Executable {
    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            Process process = Runtime.getRuntime().exec(textFormat(data, getChild(Content.class).stream().map(Content::getContent).collect(Collectors.joining(" "))));
            /*TimeoutStream timerStream = Task.getVertx().timerStream(MapUtils.getLong(attributes, "timeout", 5 * 1000L));*/
            data.cache(this, process);
            /*AtomicBoolean exeFin = new AtomicBoolean(true);
            timerStream.handler(event -> {
                log("执行超时");
                timerStream.cancel();
                process.destroyForcibly();
                toNext.fail(new TimeoutException());
                exeFin.set(false);
            });
            if (exeFin.get()) {*/
            Object result;
            switch (MapUtils.getString(attributes, "output", "")) {
                case "line":
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    result = new LinkedList<String>();
                    String txt;
                    log("==>      执行结果");
                    while (Objects.nonNull(txt = bufferedReader.readLine())) {
                        ((List) result).add(txt);
                        log("==>      " + txt);
                    }
                    break;
                default:
                    result = StreamUtils.copyToString(process.getInputStream(), Charset.defaultCharset());
                    log("执行结果");
                    log(result);
                    break;
            }
            toNext.complete(result);
                /*timerStream.cancel();
            }*/
        } catch (IOException e) {
            toNext.fail(e);
        }
//        super.start(data, preUnitResult, toNext);
    }

    @Override
    protected Future<Void> destroy(TaskRequest taskRequest) {
        Process process = taskRequest.cache(this);
        if (Objects.nonNull(process)) {
            long pId = process.pid();
            try {
                process.destroyForcibly();
            } catch (Throwable e) {
                try {
                    Runtime.getRuntime().exec("kill -9 " + pId);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        return super.destroy(taskRequest);
    }
}
