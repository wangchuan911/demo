package org.welisdoom.task.xml.entity;

import io.vertx.core.http.HttpConnection;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @Classname Http
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "http", parentTagTypes = Executable.class)
public class Http extends Unit implements Executable {
    @Override
    protected void execute(Map<String, Object> data) {
        String body = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data);
        System.out.println(body);
        if (true)
            return;
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) new URL(attributes.get("url")).openConnection();
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.connect();
            switch (attributes.get("output")) {
                case "stream":
                    Closeable closeable = httpConnection.getOutputStream();
                    try (closeable) {
                        data.put("parent.stream", closeable);
                        children.stream().filter(unit -> unit instanceof Executable).findFirst().orElse(new Unit()).execute(data);
                    }
                    break;
                default:
                    data.put("output", StreamUtils.copyToString(httpConnection.getInputStream(), Charset.forName("utf-8")));
                    execute(data, Executable.class);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (httpConnection == null) return;
            try {
                httpConnection.disconnect();
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
    }

    @Tag(value = "body", parentTagTypes = Http.class)
    public static class Body extends Unit implements Script {
        @Override
        public String getScript(Map<String, Object> data) {
            return content + Script.getScript(data, children.stream()
                    .filter(unit -> unit instanceof Script)
                    .map(unit -> (Script) unit));
        }
    }
}
