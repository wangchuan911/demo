package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.NoStackTraceThrowable;
import org.apache.commons.collections4.MapUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Http
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "vertx-http", parentTagTypes = Executable.class, desc = "http请求")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "url", desc = "请求地址")
@Attr(name = "output", desc = "输出方式:stream流;json;string(默认)")
public class VertxHttp extends Http {

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        String inputBody = getChild(Body.class).stream().findFirst().orElse(new Body()).getScript(data, "").trim();
        log(LogUtils.styleString("params:", 42, 2, inputBody));
        log(data, "不记录", "不记录");


        try {
            HttpClient client = data.cache(this, () -> Task.getVertx().createHttpClient());
            client.request(HttpMethod.valueOf(attributes.getOrDefault("method", "POST")), getUrl(data)).compose(httpClientRequest -> {
                for (Header header : getChild(Header.class)) {
                    httpClientRequest.putHeader(header.getName(), header.getContent());
                    log(String.format("header: %s = %s", header.getName(), header.getContent()));
                }
                return httpClientRequest.send(inputBody);
            }).compose(httpClientResponse ->
                    httpClientResponse.body().compose(buffer -> {
                        String outputBody = buffer.toString("utf-8");
                        if (httpClientResponse.statusCode() == 200) {
                            Object result;
                            switch (attributes.getOrDefault("output", "default")) {
                                case "stream":
                                    result = new ByteArrayInputStream(outputBody.getBytes(StandardCharsets.UTF_8));
                                    break;
                                case "json":
                                    result = JSON.parse(outputBody);
                                    break;
                                default:
                                    result = outputBody;
                                    break;
                            }
                            log(data, inputBody, outputBody);
                            return httpClientResponse.end().onComplete(ev -> Future.succeededFuture(result));
                        } else {
                            return httpClientResponse.end().onComplete(ev -> Future.failedFuture(outputBody));
                        }
                    })
            ).onComplete(event -> complete(event, toNext));
        } catch (Throwable e) {
            log(data, inputBody, e);
            toNext.fail(e);
        }
    }

}
