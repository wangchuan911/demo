package org.welisdoon.web.common.web;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@DataObject
public class Requset {
    private String method;
    private String service;
    private String body;
    private String session;
    private String params;
    private int mode;


    public Requset() {

    }

    public Requset(JsonObject jsonObject) {
        method = jsonObject.getString("method");
        service = jsonObject.getString("service");
        body = jsonObject.getString("body");
        session = jsonObject.getString("session");
        params = jsonObject.getString("params");
        mode = jsonObject.getInteger("mode");
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
        if (params != null && !CollectionUtils.isEmpty(params.entries())) {
            this.setParams(Json.encode(params.entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
        }
        return this;
    }

    public int getMode() {
        return mode;
    }

    public Requset setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public static final int SIMPLE_REQUEST = 2, UPLOAD_FILE = 3, UPLOAD_FILES = 4, WECHAT = 5;

    public static Requset newInstance(RoutingContext context, RequsetOption option) {
        int mode;
        String type = context.request().getParam(option.getRequestType());
        switch (StringUtils.isEmpty(type) ? "" : type) {
            case "UPS":
                mode = UPLOAD_FILES;
                break;
            case "UP":
                mode = UPLOAD_FILE;
                break;
            default:
                mode = SIMPLE_REQUEST;
        }
        return newInstance(mode, context, option);
    }

    public static Requset newInstance(int mode, RoutingContext context, RequsetOption option) {

        HttpServerRequest httpServerRequest = context.request();
        MultiMap multiMap = httpServerRequest.params();
        Requset requset = new Requset();
        String body = null;
        String method = null;
        String service = multiMap.get(option.getServerNameKey()) + "Service";
        switch (mode) {
            case SIMPLE_REQUEST: {
                method = multiMap.get(option.getMethodNameKey());
                method = method != null && !"null".equals(method) && method.length() != 0 ? method : "handle";
                body = context.getBodyAsString();
            }
            break;
            case UPLOAD_FILES: {
                Set<FileUpload> fileUploads = context.fileUploads();
                method = "uploadFiles";
                body = new JsonArray()
                        .add(fileUploads.stream().map(Requset::loadFileToJson).collect(Collectors.toList()))
                        .add(formToJson(httpServerRequest)).toString();
            }
            break;
            case UPLOAD_FILE: {
                Set<FileUpload> fileUploads = context.fileUploads();
                FileUpload fileUpload = fileUploads.iterator().next();
                method = "uploadFile";
                body = (new JsonArray()
                        .add(loadFileToJson(fileUpload))
                        .add(formToJson(httpServerRequest)).toString());

            }
            break;
        }

        requset.setMethod(method).setBody(body).setService(service).putParams(httpServerRequest.params())
                .putSession(context.session()).setMode(mode);
        return requset;
    }

    static JsonObject loadFileToJson(FileUpload fileUpload) {
        return new JsonObject()
                .put("uploadedFileName", fileUpload.uploadedFileName())
                .put("name", fileUpload.name())
                .put("charSet", fileUpload.charSet())
                .put("contentType", fileUpload.contentType())
                .put("size", fileUpload.size())
                .put("fileName", fileUpload.fileName())
                .put("contentTransferEncoding", fileUpload.contentTransferEncoding());
    }

    static JsonObject formToJson(HttpServerRequest httpServerRequest) {
        return new JsonObject(httpServerRequest
                .formAttributes()
                .entries()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
