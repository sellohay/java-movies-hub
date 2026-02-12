package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class MoviesApiTest {

    protected static final String BASE = "http://localhost:8080";
    protected static MoviesServer server;
    protected static MoviesStore store;
    protected static HttpClient client;
    protected static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new Gson();
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @DisplayName("Проверка обработки некорректного метода")
    @Test
    public void testApi_IncorrectMethod() throws IOException, InterruptedException {

        String requestBody = """
                    "title": "some body"
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(405, resp.statusCode(), "PUT /movies должен вернуть 405");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Некорректный метод", errorResponse.getError());
    }

}