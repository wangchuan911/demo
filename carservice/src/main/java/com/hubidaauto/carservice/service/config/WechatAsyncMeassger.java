package com.hubidaauto.carservice.service.config;

import io.vertx.ext.web.client.WebClient;
import org.springframework.stereotype.Component;


public class WechatAsyncMeassger {
    WebClient webClient;
    String url;

    public WechatAsyncMeassger(WebClient webClient, String url) {
        this.webClient = webClient;
        this.url = url;
    }

    public void pushMessage() {
        webClient.postAbs(url);
    }
}
