package bg.sofia.uni.fmi.mjt.bookmarksmanager.api;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ShortenLinkAPIHandler {
    private static final String BITLY_API_KEY = System.getenv("BITLY_ENV_KEY");
    private static final String API_REQUEST_URI = "https://api-ssl.bitly.com/v4/shorten";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer ";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_FORMAT = "application/json";
    private static final String ORIGINAL_BOOKMARK_KEY = "long_url";
    private static final String BITLY_API_DOMAIN = "bit.ly";
    private static final String BITLY_API_DOMAIN_KEY= "domain";
    private static final String SHORTENED_LINK_KEY = "link";

    private static final int SUCCESS_CODE = 200;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    static {
        if (BITLY_API_KEY == null || BITLY_API_KEY.isBlank()) {
            ExceptionsLogger.logClientException(new
                    IllegalStateException("Missing BITLY_ENV_KEY" +
                    " environment variable!"));
            throw new IllegalStateException("Can not use URL shortening " +
                    "since API key for Bitly is missing!.");
        }
    }

    public static String getShortenedLink(String originalUrl) {
        Gson gson = new Gson();
        String requestPayload = gson.toJson(Map.of(
                ORIGINAL_BOOKMARK_KEY, originalUrl,
                BITLY_API_DOMAIN_KEY, BITLY_API_DOMAIN));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_REQUEST_URI))
                .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE + BITLY_API_KEY)
                .header(CONTENT_TYPE_HEADER, CONTENT_FORMAT)
                .POST(HttpRequest.BodyPublishers.ofString(requestPayload))
                .build();

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == SUCCESS_CODE) {
                Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
                return (String) responseBody.get(SHORTENED_LINK_KEY);
            } else {
                ExceptionsLogger.logClientException(new RuntimeException("Could not send" +
                        " a request to Bitly API. Status code: " + response.statusCode()));
                return originalUrl;
            }
        } catch (IOException | InterruptedException e) {
            ExceptionsLogger.logClientException(e);
            return originalUrl;
        }
    }
}
