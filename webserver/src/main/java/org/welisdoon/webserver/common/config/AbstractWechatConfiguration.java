package org.welisdoon.webserver.common.config;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
    private String mchKey;
    private String appName;
    private String address;
    private Path path;
    private String netIp;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getMchKey() {
        return mchKey;
    }

    public void setMchKey(String mchKey) {
        this.mchKey = mchKey;
    }

    public String getNetIp() {
        if (StringUtils.isEmpty(this.netIp)) {
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                this.netIp = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return netIp;
    }

    public void setNetIp(String netIp) {
        this.netIp = netIp;
    }

    public static class Path {
        String pay;
        String app;

        public String getPay() {
            return pay;
        }

        public void setPay(String pay) {
            this.pay = pay;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }
    }
}
