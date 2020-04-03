package com.hubidaauto.carservice.service.config;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;


public class WechatAsyncMeassger {

    private static final Logger logger = LoggerFactory.getLogger(WechatAsyncMeassger.class);
    WebClient webClient;
    String url;
    String token;

    public WechatAsyncMeassger(WebClient webClient, String url) {
        this.webClient = webClient;
        this.url = url;
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

    public void pushMessage(Object o) {
        final String url = String.format("%s%s", this.url, this.token);
        webClient.postAbs(url).sendJson(o, httpResponseAsyncResult -> {
            if (httpResponseAsyncResult.succeeded()) {
                String res = httpResponseAsyncResult.result().bodyAsString();
                if (res.indexOf("\"errcode\":0") == -1) {
                    printErr(url, o, res);
                }
            } else {
                printErr(url, o, httpResponseAsyncResult.cause().getMessage());
                logger.error(httpResponseAsyncResult.cause().getMessage(), httpResponseAsyncResult.cause());
            }
        });
    }

    private void printErr(String url, Object o, String res) {
        logger.error(String.format("\nURL===>%s\nREQ===>%s\nRESP===>%s", url, JsonObject.mapFrom(o).toString(), res));
    }
}
