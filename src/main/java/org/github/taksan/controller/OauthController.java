package org.github.taksan.controller;

import org.github.taksan.application.GoogleApiApplication;
import spark.Request;

import java.io.IOException;
import java.util.function.Consumer;

public class OauthController {
    private final GoogleApiApplication application;

    public OauthController(GoogleApiApplication application) {
        this.application = application;
    }

    public void validateAuthorization(Request request, Consumer<String> userLogged) throws IOException {
        final String localUsername = request.queryParams("state");
        final String code = request.queryParams("code");
        application.authenticate(localUsername, code);
        userLogged.accept(localUsername);
    }
}
