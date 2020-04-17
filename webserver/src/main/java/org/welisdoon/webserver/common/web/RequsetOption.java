package org.welisdoon.webserver.common.web;


public class RequsetOption {
    String serverNameKey;
    String methodNameKey;
    String requestType;

    public String getServerNameKey() {
        return serverNameKey;
    }

    public RequsetOption setServerNameKey(String serverNameKey) {
        this.serverNameKey = serverNameKey;
        return this;
    }

    public String getMethodNameKey() {
        return methodNameKey;
    }

    public RequsetOption setMethodNameKey(String methodNameKey) {
        this.methodNameKey = methodNameKey;
        return this;
    }

    public String getRequestType() {
        return requestType;
    }

    public RequsetOption setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }
}
