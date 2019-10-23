package pers.welisdoon.webserver.common.web;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
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

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getException() {
        return exception;
    }

    public void setException(Object exception) {
        this.exception = exception;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
