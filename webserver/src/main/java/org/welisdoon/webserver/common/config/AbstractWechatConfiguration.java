package org.welisdoon.webserver.common.config;

import java.util.Map;

public class AbstractWechatConfiguration {
    private String appID;
    private String appsecret;
    private String token;
    private String key;
    private Integer afterUpdateTokenTime;
    private Map urls;
    private Map schedul;
    private String mchId;
    private String appName;

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getAfterUpdateTokenTime() {
        return afterUpdateTokenTime;
    }

    public void setAfterUpdateTokenTime(Integer afterUpdateTokenTime) {
        this.afterUpdateTokenTime = afterUpdateTokenTime;
    }

    public Map getUrls() {
        return urls;
    }

    public void setUrls(Map urls) {
        this.urls = urls;
    }

    public Map getSchedul() {
        return schedul;
    }

    public void setSchedul(Map schedul) {
        this.schedul = schedul;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
