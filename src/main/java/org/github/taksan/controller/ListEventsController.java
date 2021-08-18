package org.github.taksan.controller;

import org.github.taksan.application.UserNotAuthorizedOnGoogleException;
import org.github.taksan.application.GoogleApiApplication;
import spark.ModelAndView;
import spark.Response;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class ListEventsController {
    private final GoogleApiApplication application;
    private ThymeleafTemplateEngine engine;

    public ListEventsController(GoogleApiApplication application, ThymeleafTemplateEngine engine) {
        this.application = application;
        this.engine = engine;
    }

    public String listEvents(Response res) throws IOException {
        try {
            final HashMap<Object, Object> model = new HashMap<>();
            model.put("name", application.getLoggedUserName().orElse("who are you?"));
            model.put("list", application.listCalendarEvents());
            return engine.render(new ModelAndView(model, "events"));
        } catch (UserNotAuthorizedOnGoogleException ex) {
            final Optional<String> localUserName = application.getLoggedUserName();
            localUserName
                .ifPresentOrElse(
                        user -> res.redirect("/authenticate?username=" + user),
                        ()   -> res.redirect("/"));
            return "";
        }
    }
}
