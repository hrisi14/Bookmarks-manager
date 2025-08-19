package bg.sofia.uni.fmi.mjt.bookmarksmanager.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class ShortenLinkAPIHandlerTest {

    private HttpClient mockClient;
    private HttpResponse mockResponse;

    @BeforeEach
    void setUp() throws Exception {
        mockClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);

        Field clientField = ShortenLinkAPIHandler.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(null, mockClient);
    }

    @Test
    void testGetShortenedLink_successfulResponse() throws Exception {
        String shortenedUrl = "http://bit.ly/abc123";
        String jsonResponse = new Gson().toJson(
                java.util.Map.of("link", shortenedUrl));

        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn(jsonResponse);
        Mockito.when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String result = ShortenLinkAPIHandler.getShortenedLink("http://example.com");
        assertEquals(shortenedUrl, result);
    }

    @Test
    void testGetShortenedLink_failureResponse() throws Exception {
        Mockito.when(mockResponse.statusCode()).thenReturn(500);
        Mockito.when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String original = "http://example.com";
        String result = ShortenLinkAPIHandler.getShortenedLink(original);
        assertEquals(original, result);
    }

    @Test
    void testGetShortenedLink_ioException() throws Exception {
        Mockito.when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network error"));

        String original = "http://example.com";
        String result = ShortenLinkAPIHandler.getShortenedLink(original);
        assertEquals(original, result);
    }
}
