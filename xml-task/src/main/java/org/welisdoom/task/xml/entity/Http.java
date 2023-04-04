package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Http
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "http", parentTagTypes = Executable.class)
@Attr(name = "id", desc = "唯一标识")
public class Http extends Unit implements Executable {
    /*@Override
    protected void execute(TaskRequest data) {
        String body = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data.getBus(), "").trim();
        System.out.println(body);
        if (true) {
            data.next(null);
            return;
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) new URL(attributes.get("url")).openConnection();
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            for (Header header : getChild(Header.class)) {
                httpConnection.setRequestProperty(header.getId(), header.getContent());
            }
            httpConnection.connect();
            switch (attributes.get("output")) {
                case "stream":
                    Closeable closeable = httpConnection.getOutputStream();
                    try (closeable) {
                        data.setBus(this, "@stream", closeable);
                        children.stream().filter(unit -> unit instanceof Executable).findFirst().orElse(new Unit()).execute(data);
                    }
                    break;
                default:
                    data.setBus(this, "@stream", StreamUtils.copyToString(httpConnection.getInputStream(), Charset.forName("utf-8")));
                    execute(data, Executable.class);
            }
            data.next(null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (httpConnection != null) {
                try {
                    httpConnection.disconnect();
                } catch (Throwable e1) {
                    e1.printStackTrace();
                }
            }
        }
    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        String body = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data.getBus(), "").trim();
        System.out.println(body);
        if (true) {
            toNext.complete();
            return;
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) new URL(attributes.get("url")).openConnection();
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            for (Header header : getChild(Header.class)) {
                httpConnection.setRequestProperty(header.getId(), header.getContent());
            }
            httpConnection.connect();
            Future<Object> future;
            switch (attributes.get("output")) {
                case "stream":
                    future = startChildUnit(data, httpConnection.getOutputStream(), Executable.class);
                    break;
                default:
                    future = startChildUnit(data, StreamUtils.copyToString(httpConnection.getInputStream(), Charset.forName("utf-8")));
                    break;
            }
            future.onSuccess(toNext::complete).onFailure(toNext::fail);
        } catch (Throwable e) {
            toNext.fail(e);
        } finally {
            if (httpConnection != null) {
                try {
                    httpConnection.disconnect();
                } catch (Throwable e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Tag(value = "body", parentTagTypes = Http.class)
    public static class Body extends Unit implements Script {

        public String getScript(Map<String, Object> data, String split) {
            return children.stream()
                    .filter(unit -> unit instanceof Script)
                    .map(unit -> ((Script) unit).getScript(data, split).trim()).collect(Collectors.joining(split));
        }
    }

    @Tag(value = "header", parentTagTypes = Http.class)
    public static class Header extends Unit {

        public String getContent() {
            return MapUtils.getString(attributes, "content", "").trim();
        }
    }
}
