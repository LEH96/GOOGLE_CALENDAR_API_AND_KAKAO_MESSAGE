package com.example.demo.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.squareup.okhttp.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class CalendarService {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final java.io.File DATA_STORE_DIR = new java.io.File(TOKENS_DIRECTORY_PATH);

    public static String setEvent(String accessToken) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        System.out.println("access token: "+accessToken);
        Credential credential = new GoogleCredential().setAccessToken(accessToken);
        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        Event event = new Event()
                .setSummary("이벤트 이름")
                .setDescription("이벤트 내용")
                .setLocation("강남역");

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(System.currentTimeMillis()))
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(System.currentTimeMillis()))
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        event = service.events().insert("primary", event).execute();
        System.out.printf("이벤트가 생성되었습니다. 이벤트 ID: %s\n", event.getHtmlLink());
        return event.getHtmlLink();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = CalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        AuthorizationCodeInstalledApp authorization = new AuthorizationCodeInstalledApp(flow, receiver);
        Credential credential = authorization.authorize("user");
        return credential;
    }

    public static String getAccessTokenJsonData(String code) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://oauth2.googleapis.com/token").newBuilder();
        urlBuilder.addQueryParameter("client_id", "client_id");
        urlBuilder.addQueryParameter("client_secret", "client_secret");
        urlBuilder.addQueryParameter("code", code);
        urlBuilder.addQueryParameter("grant_type", "authorization_code");
        urlBuilder.addQueryParameter("redirect_uri", "http://localhost:8080/callback");

        RequestBody requestBody = RequestBody.create(null, new byte[0]);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(requestBody)
                .build();

        try (ResponseBody response = client.newCall(request).execute().body()) {
            String responseBody = response.string();
            File file = new File(TOKENS_DIRECTORY_PATH + "/token.json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            writer.write(responseBody);
            writer.flush();
            writer.close();

            return "/token";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";
    }

    public static String readAccessTokenFromFile() {
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + File.separator + "tokens/token.json";
        String accessToken = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filePath));
            accessToken = (String) json.get("access_token");

            if (accessToken == null) {
                System.out.println("Access Token not found in the JSON file.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }
}