package org.github.taksan.application;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CalendarService {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final Calendar service;

    public CalendarService(NetHttpTransport httpTransport, Credential credentials) {
        service = new Calendar.Builder(httpTransport, JSON_FACTORY, credentials)
            .setApplicationName(GoogleApiApplication.APPLICATION_NAME)
            .build();
    }

    public List<String> listEvents() throws IOException {
        final DateTime now = new DateTime(System.currentTimeMillis());
        final Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();

        final List<String> eventNames = new LinkedList<>();
        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null)
                start = event.getStart().getDate();

            eventNames.add(String.format("\"%s (%s)\"", event.getSummary(), start));
        }
        return eventNames;
    }
}
