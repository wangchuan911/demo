package org.welisdoon.web.common.web;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Response {
    Object result;
    Object exception;
    Object error;

    public Response() {

    }

    public Response(JsonObject jsonObject) {
        result = jsonObject.getValue("result");
        exception = jsonObject.getValue("exception");
        error = jsonObject.getValue("error");
    }

    public Object getResult() {
        return result;
    }

    public Response setResult(Object result) {
        this.result = result;
        return this;
    }

    public Object getException() {
        return exception;
    }

    public Response setException(Object exception) {
        this.exception = exception;
        return this;
    }

    public Object getError() {
        return error;
    }

    public Response setError(Object error) {
        this.error = error;
        return this;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
