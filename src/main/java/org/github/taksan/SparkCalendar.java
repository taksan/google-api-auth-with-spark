package org.github.taksan;

import org.github.taksan.application.GoogleApiApplication;
import org.github.taksan.controller.AuthorizationController;
import org.github.taksan.controller.ListEventsController;
import org.github.taksan.controller.OauthController;
import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import static spark.Spark.get;
import static spark.Spark.port;

public class SparkCalendar {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        final ThymeleafTemplateEngine engine = new ThymeleafTemplateEngine();
        var application = GoogleApiApplication.initialize();

        port(8888);

        setupIndex(engine);
        setupAuthentication(application);
        setupOauthCallback(application);
        setupCalendarListEndpoint(application, engine);
    }

    private static void setupIndex(ThymeleafTemplateEngine engine) {
        get("/", (req, res) -> engine.render(new ModelAndView(new HashMap<>(), "index")));
    }

    private static void setupAuthentication(GoogleApiApplication application) {
        final AuthorizationController authenticationHandler = new AuthorizationController(application);
        get("/authenticate", (request, response) ->
            authenticationHandler.ifAuthorized(request, response, () ->
                response.redirect("/list")
            ));
    }

    private static void setupOauthCallback(GoogleApiApplication application) {
        final var oauthCallbackHandler = new OauthController(application);
        get("/Callback", (request, response) -> {
           oauthCallbackHandler.validateAuthorization(request, username ->
               response.redirect("/list?username=" + username)
           );
           return "";
        });
    }

    private static void setupCalendarListEndpoint(GoogleApiApplication application, ThymeleafTemplateEngine engine) {
        get("/list", (req, res) -> new ListEventsController(application, engine).listEvents(res));
    }
}
