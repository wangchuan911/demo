package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.NoStackTraceThrowable;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.LogUtils;
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
    protected String getUrl(TaskRequest data) {
        return textFormat(data, attributes.get("url"));
    }

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        String inputBody = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data, "").trim(),
                outputBody = "empty data";
        log(LogUtils.styleString("params:", 42, 2, inputBody));
        HttpURLConnection httpConnection = null;
        try {
            /*if (true) {
                toNext.complete();
                return;
            }*/
            httpConnection = (HttpURLConnection) new URL(getUrl(data)).openConnection();
            // 打开和URL之间的连接

            // 发送POST请求必须设置如下两行
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setRequestMethod(attributes.getOrDefault("method", "POST"));    // POST方法


            for (Header header : getChild(Header.class)) {
                httpConnection.setRequestProperty(header.getName(), header.getContent());
                log(String.format("header: %s = %s", header.getName(), header.getContent()));
            }

            OutputStream output = httpConnection.getOutputStream();
            try (output) {
                StreamUtils.copy(inputBody, Charset.forName("utf-8"), output);
            }

            httpConnection.connect();
            if (httpConnection.getResponseCode() == 200) {
                InputStream input = httpConnection.getInputStream();
                Object result;
            /*try (input) {
                StreamUtils.copyToString(input, Charset.forName("utf-8"));
            }*/
                switch (attributes.getOrDefault("output", "default")) {
                    case "stream":
                        outputBody = "data is stream";
                        result = (input);
                        break;
                    case "json":
                        result = (JSON.parse(outputBody = StreamUtils.copyToString(input, Charset.forName("utf-8"))));
                        break;
                    default:
                        result = (outputBody = StreamUtils.copyToString(input, Charset.forName("utf-8")));
                        break;
                }
                log(data, inputBody, outputBody);
                toNext.complete(result);
            } else {
                InputStream input = httpConnection.getErrorStream();
                throw new NoStackTraceThrowable(StreamUtils.copyToString(input, Charset.forName("utf-8")));
            }
        } catch (Throwable e) {
            log(data, inputBody, e);
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

    protected void log(TaskRequest data, String input, Throwable e) {
        if ("true".equals(attributes.get("is-log"))) {
            try {
                ObjectUtils.getMapValueOrNewSafe(data.getBus(), attributes.get("id"), HashMap::new);
                log(data, input, /*ExceptionUtils.getStackTrace(e)*/e.getMessage());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    protected void log(TaskRequest data, String input, String output) {
        if ("true".equals(attributes.get("is-log"))) {
            try {
                Map log = (Map) ObjectUtils.getMapValueOrNewSafe(data.getBus(), attributes.get("id"), HashMap::new);
                log.put("url", getUrl(data));
                log.put("input", input);
                log.put("output", output);
                log(log);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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
