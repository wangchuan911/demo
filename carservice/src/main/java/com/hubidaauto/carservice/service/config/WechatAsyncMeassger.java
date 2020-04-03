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
        logger.info(JsonObject.mapFrom(o).toString());
        logger.info(url);
        webClient.postAbs(url).sendJson(o, httpResponseAsyncResult -> {
            if (httpResponseAsyncResult.succeeded()) {
                logger.info(httpResponseAsyncResult.result().bodyAsString());
            } else {
                logger.error(httpResponseAsyncResult.cause().getMessage(), httpResponseAsyncResult.cause());
            }
        });
    }
}
