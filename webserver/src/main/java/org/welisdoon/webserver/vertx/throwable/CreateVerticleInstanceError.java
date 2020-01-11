package org.welisdoon.webserver.vertx.throwable;

public class CreateVerticleInstanceError extends Error {
    public CreateVerticleInstanceError(int code) {
        super(CreateVerticleInstanceError.getCode(code));

    }

    private static String getCode(int code) {
        String error;
        switch (code) {
            case 0:
                error = "create instance must single !";
                break;
            default:
                error = "create instance errror";
                break;
        }
        return error;
    }
}
