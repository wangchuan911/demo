package org.welisdoon.webserver.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;

import java.util.HashMap;
import java.util.Map;


public class WechatAsyncMeassger {

    private static final Logger logger = LoggerFactory.getLogger(WechatAsyncMeassger.class);
    WebClient webClient;
    String url;
    String token;
    Handler<HttpResponse<Buffer>> success = null;
    Handler<Throwable> fail = null;
    final static Map<Class<?>, WechatAsyncMeassger> MAP = new HashMap(4);

    public WechatAsyncMeassger(Class<? extends AbstractWechatConfiguration> cls, WebClient webClient, String url) {
        this.webClient = webClient;
        this.url = url;
        if (MAP.containsKey(cls))
            throw new RuntimeException(String.format("%s is exists", cls.getName()));
        MAP.put(cls, this);
    }

    public static WechatAsyncMeassger get(Class<? extends AbstractWechatConfiguration> cls) {
        return MAP.get(cls);
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setSuccess(Handler<HttpResponse<Buffer>> success) {
        this.success = success;
    }

    public void setFail(Handler<Throwable> fail) {
        this.fail = fail;
    }

    public void pushMessage(Object o) {
        final String url = String.format("%s%s", this.url, this.token);
        webClient.postAbs(url).sendJson(o, httpResponseAsyncResult -> {
            if (httpResponseAsyncResult.succeeded()) {
               /* String res = httpResponseAsyncResult.result().bodyAsString();
                if (res.indexOf("\"errcode\":0") == -1) {
                    printErr(url, o, res);
                }*/
                if (this.success != null)
                    this.success.handle(httpResponseAsyncResult.result());
                else
                    print(url, o, httpResponseAsyncResult.result().bodyAsString());
            } else {
                /*printErr(url, o, httpResponseAsyncResult.cause().getMessage());
                logger.error(httpResponseAsyncResult.cause().getMessage(), httpResponseAsyncResult.cause());*/
                if (this.fail != null)
                    this.fail.handle(httpResponseAsyncResult.cause());
                else
                    print(url, o, httpResponseAsyncResult.result().bodyAsString());
            }
        });
    }

    public void print(String url, Object o, String res) {
        logger.warn(String.format("\nURL===>%s\nREQ===>%s\nRESP===>%s", url, JsonObject.mapFrom(o).toString(), res));
    }
}
