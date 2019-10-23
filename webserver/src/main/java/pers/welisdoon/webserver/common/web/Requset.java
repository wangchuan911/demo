package pers.welisdoon.webserver.common.web;

import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;

@DataObject
public class Requset {
    String method;
    String service;
    Object body;
    JsonObject session;
    JsonObject params;


    public Requset() {

    }

    public Requset(JsonObject jsonObject) {
        method = jsonObject.getString("method");
        service = jsonObject.getString("service");
        body = jsonObject.getValue("body");
        session = jsonObject.getJsonObject("session");
        params = jsonObject.getJsonObject("params");
    }

    private <T> T putValue(Object o, Class<T> t) {
        if (o instanceof JsonObject) {
            if (t == MultiMap.class) {
                Map map = ((JsonObject) o).getMap();
                return (T) MultiMap.caseInsensitiveMultiMap().addAll((Map<String, String>) map);
            }
            else {
                return null;
            }
        }
        else {
            return (T) o;
        }
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

    public Object getBody() {
        return body;
    }

    public Requset setBody(Object body) {
        this.body = body;
        return this;
    }

    public JsonObject getSession() {
        return session;
    }

    public Requset setSession(JsonObject session) {
        this.session = session;
        return this;
    }

    public Requset putSession(Session session) {
        if (session != null)
            this.session = JsonObject.mapFrom(session.data());
        return this;
    }

    public JsonObject getParams() {
        return params;
    }

    public Requset setParams(JsonObject params) {
        this.params = params;
        return this;
    }

    public Requset putParams(MultiMap params) {
        this.params = JsonObject.mapFrom(params.entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return this;
    }
}
