package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Future;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.LogUtils;
import org.welisdoon.common.ObjectUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    protected String getUrl(TaskInstance data) {
        return UnitType.textFormat(data, attributes.get("url"));
    }

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        String inputBody = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data, "").trim();
        log(LogUtils.styleString("params:", 42, 2, inputBody));
        addLog(data, "不记录", "不记录");
        return Task.getVertx().executeBlocking(() -> {
            HttpURLConnection httpConnection = null;
            try {
                String outputBody = "empty data";
            /*if (true) {
                toNext.complete();
                return;
            }*/
                httpConnection = ((HttpURLConnection) new URL(getUrl(data)).openConnection());
                // 打开和URL之间的连接

                // 发送POST请求必须设置如下两行
                httpConnection.setDoOutput(true);
                httpConnection.setDoInput(true);
                httpConnection.setRequestMethod(attributes.getOrDefault("method", "POST"));    // POST方法
                String contentType = "";
                for (Header header : getChild(Header.class)) {
                    httpConnection.setRequestProperty(header.getName(), header.getContent());
                    log(String.format("header: %s = %s", header.getName(), header.getContent()));
                    if (header.getName().equalsIgnoreCase("content-type")) {
                        contentType = header.getContent();
                    }
                }


                write(httpConnection, inputBody, contentType);

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
                            result = (JSON.parse(outputBody = StreamUtils.copyToString(input, StandardCharsets.UTF_8)));
                            break;
                        default:
                            result = (outputBody = StreamUtils.copyToString(input, StandardCharsets.UTF_8));
                            break;
                    }
                    addLog(data, inputBody, outputBody);
                    return result;
                } else {
                    InputStream input = httpConnection.getErrorStream();
                    throw new IllegalStateException(StreamUtils.copyToString(input, StandardCharsets.UTF_8));
                }

            } finally {
                if (httpConnection != null) {
                    try {
                        httpConnection.disconnect();
                    } catch (Throwable e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).onComplete(event -> {
            if (event.failed()) {
                addLog(data, inputBody, event.cause());
            }
        });
    }

    protected void addLog(TaskInstance data, String input, Throwable e) {
        if ("true".equals(attributes.get("is-log"))) {
            try {
                ObjectUtils.getMapValueOrNewSafe(data.getBus(), attributes.get("id"), HashMap::new);
                addLog(data, input, /*ExceptionUtils.getStackTrace(e)*/e.getMessage());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    protected void write(HttpURLConnection httpConnection, String inputBody, String contentType) throws IOException {
        switch (Optional.ofNullable(contentType).orElse("").toLowerCase(Locale.ROOT)) {
            case "content-type":
                if (contentType.contains("application/x-www-form-urlencoded")
                        && inputBody.startsWith("{") && inputBody.endsWith("{")) {
                    JSONObject object = JSON.parseObject(inputBody);
                    StringBuilder builder = new StringBuilder();
                    for (String s : object.keySet()) {
                        builder.append("&").append(s).append("=").append(URLEncoder.encode(object.getString(s), StandardCharsets.UTF_8));
                    }
                    inputBody = builder.length() == 0 ? "" : builder.substring(1);
                    log("{}:{} 入参转换 {}", "content-type", contentType, inputBody);
                } else if (contentType.contains("multipart/form-data")) {
                    String end = "\r\n";
                    String boundary = "*****";
                    String twoHyphens = "--";
                    httpConnection.setRequestProperty("ContentType", "multipart/form-data;boundary=" + boundary);
                    httpConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setUseCaches(false);
                    String[] uploadFilePaths = Arrays.stream(inputBody.split("\n"))
                            .filter(StringUtils::isNotEmpty).map(String::trim).toArray(String[]::new);
                    DataOutputStream ds = new DataOutputStream(httpConnection.getOutputStream());
                    try (ds) {
                        for (int i = 0; i < uploadFilePaths.length; i++) {
                            String uploadFile = uploadFilePaths[i];
                            String filename = uploadFile.substring(uploadFile.lastIndexOf("//") + 1);
                            ds.writeBytes(twoHyphens + boundary + end);
                            ds.writeBytes("Content-Disposition: form-data; " + "name=\"" + i + "\";filename=\"" + filename
                                    + "\"" + end);
                            ds.writeBytes(end);
                            FileInputStream fStream = new FileInputStream(uploadFile);
                            try (fStream) {
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int length = -1;
                                while ((length = fStream.read(buffer)) != -1) {
                                    ds.write(buffer, 0, length);
                                }
                                ds.writeBytes(end);
                            }
                        }
                        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                        ds.flush();
                    }
                    return;
                }
                break;
        }

        OutputStream output = httpConnection.getOutputStream();
        try (output) {
            StreamUtils.copy(inputBody, Charset.forName("utf-8"), output);
        }
    }

    protected void addLog(TaskInstance data, String input, String output) {
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

        public String getScript(TaskInstance request, String split) {
            return UnitType.textFormat(request, children.stream()
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
