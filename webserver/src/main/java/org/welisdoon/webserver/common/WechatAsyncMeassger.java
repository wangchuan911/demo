package org.welisdoon.webserver.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class WechatAsyncMeassger {

    private static final Logger logger = LoggerFactory.getLogger(WechatAsyncMeassger.class);
    WebClient webClient;
    Map url;
    String token;
    Handler<HttpResponse<Buffer>> success = null;
    Handler<Throwable> fail = null;
    final static Map<Class<?>, WechatAsyncMeassger> MAP = new HashMap(4);

    public WechatAsyncMeassger(AbstractWechatConfiguration configuration, WebClient webClient) {
        this.webClient = webClient;
        this.url = configuration.getUrls();
        Class<?> cls = configuration.getClass();
        if (MAP.containsKey(cls))
            throw new RuntimeException(String.format("%s is exists", cls.getName()));
        if (cls.getName().indexOf("EnhancerBySpringCGLIB") > 0) {
            cls = cls.getSuperclass();
        }
        MAP.put(cls, this);
    }

    public static WechatAsyncMeassger get(Class<? extends AbstractWechatConfiguration> cls) {
        return MAP.get(cls);
    }

    public WechatAsyncMeassger setWebClient(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    public void setUrl(String key, String url) {
        this.url.put(key, url);
    }

    public WechatAsyncMeassger setToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public WechatAsyncMeassger setSuccess(Handler<HttpResponse<Buffer>> success) {
        this.success = success;
        return this;
    }

    public WechatAsyncMeassger setFail(Handler<Throwable> fail) {
        this.fail = fail;
        return this;
    }

    public WechatAsyncMeassger post(String key, Object o) {
        final String url = urlFill(key, null);
        webClient.postAbs(url).sendJson(o, this.handler(url, o));
        return this;
    }

    public WechatAsyncMeassger get(String key, Map o) {
        final String url = urlFill(key, o);
        webClient.getAbs(url).send(this.handler(url, o));
        return this;
    }

    Handler<AsyncResult<HttpResponse<Buffer>>> handler(String url, Object o) {
        return httpResponseAsyncResult -> {
            if (httpResponseAsyncResult.succeeded()) {
                if (this.success != null)
                    this.success.handle(httpResponseAsyncResult.result());
                else
                    print(url, o, httpResponseAsyncResult.result().bodyAsString());
            } else {
                if (this.fail != null)
                    this.fail.handle(httpResponseAsyncResult.cause());
                else
                    print(url, o, httpResponseAsyncResult.result().bodyAsString());
            }
        };
    }

    String urlFill(String key, Map map) {
        StringBuilder url = new StringBuilder(this.url.get(key).toString());
        if (MapUtils.isNotEmpty(map)) {
            Set<Map.Entry> s = map.entrySet();
            s.stream().filter(entry -> entry.getValue() != null).forEach(o -> {
                replace(url, String.format("{{%s}}", o.getKey().toString()), o.getValue().toString());
            });
        }
        replace(url, "{{ACCESS_TOKEN}}", this.token);
        return url.toString();
    }

    void replace(StringBuilder url, String colmn, String value) {
        int idx;
        if ((idx = url.indexOf(colmn)) > 0) {
            url.replace(idx, idx + colmn.length(), value);
        }
    }

    public void print(String url, Object o, String res) {
        logger.warn(String.format("\nURL===>%s\nREQ===>%s\nRESP===>%s", url, JsonObject.mapFrom(o).toString(), res));
    }
}
