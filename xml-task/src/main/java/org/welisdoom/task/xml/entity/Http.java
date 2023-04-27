package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Http
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "http", parentTagTypes = Executable.class, desc = "http请求")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "url", desc = "请求地址")
@Attr(name = "output", desc = "输出方式:stream流;json;string(默认)")
public class Http extends Unit implements Executable, Copyable {
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
        String inputBody = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data, "").trim(),
                outputBody = "empty data";
        log(inputBody);
        boolean isLog = "true".equals(attributes.get("is-log"));
        HttpURLConnection httpConnection = null;
        try {
            if (true) {
                toNext.complete();
                return;
            }
            httpConnection = (HttpURLConnection) new URL(attributes.get("url")).openConnection();
            // 打开和URL之间的连接

            // 发送POST请求必须设置如下两行
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setRequestMethod("POST");    // POST方法


            for (Header header : getChild(Header.class)) {
                httpConnection.setRequestProperty(header.getId(), header.getContent());
            }

            OutputStream output = httpConnection.getOutputStream();
            try (output) {
                StreamUtils.copy(inputBody, Charset.forName("utf-8"), output);
            }

            httpConnection.connect();
            if (httpConnection.getResponseCode() == 0) {
                InputStream input = httpConnection.getInputStream();
            /*try (input) {
                StreamUtils.copyToString(input, Charset.forName("utf-8"));
            }*/
                switch (MapUtils.getString(attributes, "output", "default")) {
                    case "stream":
                        outputBody = "data is stream";
                        toNext.complete(input);
                        break;
                    case "json":
                        toNext.complete(JSON.parse(outputBody = StreamUtils.copyToString(input, Charset.forName("utf-8"))));
                        break;
                    default:
                        toNext.complete(outputBody = StreamUtils.copyToString(input, Charset.forName("utf-8")));
                        break;
                }
            } else {
                InputStream input = httpConnection.getErrorStream();
                toNext.fail(new RuntimeException(StreamUtils.copyToString(input, Charset.forName("utf-8"))));
            }
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
            if (isLog) {
                Map log = null;
                try {
                    log = (Map) ObjectUtils.getMapValueOrNewSafe(data.getBus(), attributes.get("name"), HashMap::new);
                    log.put("input", inputBody);
                    log.put("output", outputBody);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }

    @Tag(value = "body", parentTagTypes = {Http.class, Initialization.class}, desc = "post请求内容")
    public static class Body extends Unit implements Script, Copyable {

        public String getScript(TaskRequest request, String split) {
            return textFormat(request, children.stream()
                    .filter(unit -> unit instanceof Script)
                    .map(unit -> ((Script) unit).getScript(request, split).trim()).collect(Collectors.joining(split)));
        }

        @Override
        public Copyable copy() {
            return copyableUnit(this);
        }
    }

    @Tag(value = "header", parentTagTypes = {Http.class, Initialization.class}, desc = "请求头信息")
    public static class Header extends Unit implements Copyable {

        public String getContent() {
            return MapUtils.getString(attributes, "content", "").trim();
        }

        public String getName() {
            return attributes.containsKey("name") ? attributes.get("name") : getId();
        }

        @Override
        public Copyable copy() {
            return copyableUnit(this);
        }
    }
}
