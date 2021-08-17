package org.github.taksan.controller;

import org.github.taksan.application.GoogleApiApplication;
import spark.Request;
import spark.Response;

import java.io.IOException;

public class AuthorizationController {
    private final GoogleApiApplication application;

    public AuthorizationController(GoogleApiApplication application) {
        this.application = application;
    }

    public Object ifAuthorized(Request request, Response response, Runnable onAuthorized) throws IOException {
        final String localUsername = request.queryParams("username");
        application.verifyAuthorized(localUsername)
            .ifTrue(onAuthorized)
            .ifFalse(response::redirect);
        return "";
    }
}
