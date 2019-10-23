package pers.welisdoon.webserver.common.web;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@DataObject
public class Requset {
    String method;
    String service;
    JsonArray params;
    JsonObject extra;

    public Requset() {

    }

    public Requset(JsonObject jsonObject) {
        method = jsonObject.getString("method");
        service = jsonObject.getString("service");
        params = jsonObject.getJsonArray("params");
        extra = jsonObject.getJsonObject("extra");
    }

    public Requset(JsonArray jsonArray) {
        method = jsonArray.getString(0);
        service = jsonArray.getString(1);
        params = jsonArray.getJsonArray(2);
        extra = jsonArray.getJsonObject(2);
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

    public JsonArray getParams() {
        return params;
    }

    public Requset setParams(JsonArray params) {
        this.params = params;
        return this;
    }

    public JsonObject getExtra() {
        return extra;
    }

    public Requset setExtra(JsonObject extra) {
        this.extra = extra;
        return this;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
