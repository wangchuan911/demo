package org.welisdoon.webserver.common.web;

import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.MultiMap;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;
import org.springframework.util.StringUtils;

@DataObject
public class Requset {
    private String method;
    private String service;
    private String body;
    private String session;
    private String params;


    public Requset() {

    }

    public Requset(JsonObject jsonObject) {
        method = jsonObject.getString("method");
        service = jsonObject.getString("service");
        body = jsonObject.getString("body");
        session = jsonObject.getString("session");
        params = jsonObject.getString("params");
    }

    public Object bodyAsJson() {
        return Json.decodeValue(this.body);
    }

    public Object sessionAsJson() {
        return Json.decodeValue(this.session);
    }

    public Object paramsAsJson() {
        return Json.decodeValue(this.params);
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public String getMethod() {
        return method;
    }

    public Requset setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getService() {
        return service;
    }

    public Requset setService(String service) {
        this.service = service;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Requset setBody(String body) {
        this.body = body;
        return this;
    }

    public String getSession() {
        return this.session;
    }

    public Requset setSession(String session) {
        this.session = session;
        return this;
    }

    public Requset putSession(Session session) {
        if (session != null) {
            this.setSession(JsonObject.mapFrom(session.data()).toString());
        }
        return this;
    }

    public String getParams() {
        return this.params;
    }

    public Requset setParams(String params) {
        this.params = params;
        return this;
    }

    public Requset putParams(MultiMap params) {
        if (params != null) {
            this.setParams(Json.encode(params));
        }
        return this;
    }
}
