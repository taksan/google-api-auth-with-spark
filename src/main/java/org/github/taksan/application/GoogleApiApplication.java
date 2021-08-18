package org.github.taksan.application;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GoogleApiApplication {
    public static final String APPLICATION_NAME = "Google Calendar API Java Spark Quickstart";

    public static final String CALLBACK_URI = "http://localhost:8888/Callback";
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final NetHttpTransport httpTransport;

    private Credential loggedUserCredential;
    private final GoogleAuthorizationCodeFlow flow;
    private String loggedUser;

    public static GoogleApiApplication initialize() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final GoogleClientSecrets clientSecrets = loadClientSecretsOrCry();
        final GoogleAuthorizationCodeFlow authorizationFlow = setupAuthorizationCodeFlow(httpTransport, clientSecrets);

        return new GoogleApiApplication(httpTransport, authorizationFlow);
    }

    private GoogleApiApplication(NetHttpTransport httpTransport, GoogleAuthorizationCodeFlow authorizationFlow) {
        this.httpTransport = httpTransport;
        this.flow = authorizationFlow;
    }

    public void authenticate(String username, String code) throws IOException {
        final GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        this.loggedUserCredential = flow.createAndStoreCredential(tokenResponse, username);
        setAuthenticatedUser(username);
    }

    public AuthorizedResponse verifyAuthorized(String localUsername) throws IOException {
        loadCredential(localUsername);
        if (isUserLoggedIn())
            return new UserAuthorized(localUsername);
        return new NotAuthorized(localUsername);
    }

    public List<String> listCalendarEvents() throws IOException, UserNotAuthorizedOnGoogleException {
        if(isUserLoggedIn())
            return new CalendarService(httpTransport, loggedUserCredential).listEvents();

        throw new UserNotAuthorizedOnGoogleException();
    }

    private void loadCredential(String userName) throws IOException {
        this.loggedUserCredential = flow.loadCredential(userName);
    }

    private String getAuthorizationUrl(String userName) {
        final GoogleAuthorizationCodeRequestUrl codeRequestUrl = flow
                .newAuthorizationUrl()
                .setState(userName)
                .setRedirectUri(CALLBACK_URI);

        return codeRequestUrl.build();
    }

    private boolean isUserLoggedIn() {
        return this.loggedUserCredential != null;
    }

    private void setAuthenticatedUser(String username) {
        this.loggedUser = username;
    }

    private static GoogleAuthorizationCodeFlow setupAuthorizationCodeFlow(NetHttpTransport HTTP_TRANSPORT, GoogleClientSecrets clientSecrets) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    private static GoogleClientSecrets loadClientSecretsOrCry() throws IOException {
        final InputStream credentialsStream = new FileInputStream(CREDENTIALS_FILE_PATH);
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(credentialsStream));
    }

    public Optional<String> getLoggedUserName() {
        return Optional.ofNullable(loggedUser);
    }

    public interface AuthorizedResponse {
        AuthorizedResponse ifTrue(Runnable runIfTrue);
        void ifFalse(Consumer<String> authorizationUrlCallback);
    }

    public class UserAuthorized implements AuthorizedResponse {
        private final String localUsername;

        public UserAuthorized(String localUsername) {
            this.localUsername = localUsername;
        }

        @Override
        public AuthorizedResponse ifTrue(Runnable runIfTrue) {
            setAuthenticatedUser(localUsername);
            runIfTrue.run();
            return this;
        }

        @Override
        public void ifFalse(Consumer<String> authorizationUrlCallback) {
        }
    }

    class NotAuthorized implements AuthorizedResponse {
        private final String localUsername;

        public NotAuthorized(String localUsername) {
            this.localUsername = localUsername;
        }

        @Override
        public AuthorizedResponse ifTrue(Runnable runIfTrue) {
            return this;
        }

        @Override
        public void ifFalse(Consumer<String> authorizationUrlCallback) {
            authorizationUrlCallback.accept(getAuthorizationUrl(localUsername));
        }
    }
}
