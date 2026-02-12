package ru.practicum.moviehub.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesGetByIdTest extends MoviesApiTest {

    @DisplayName("возвращает фильм по существующему id")
    @Test
    public void testGetMovie_CorrectId() throws IOException, InterruptedException {

        store.addMovie("Гадкий Я", 2010);
        store.addMovie("Муму", 1959);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/1"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies/1 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Movie respMovie = gson.fromJson(resp.body(), Movie.class);
        assertEquals(1, respMovie.getId());
        assertEquals("Гадкий Я", respMovie.getTitle());
        assertEquals(2010, respMovie.getYear());

    }

    @DisplayName("возвращает ошибку, если фильм не найден")
    @Test
    public void testGetMovie_WrongId() throws IOException, InterruptedException {

        store.addMovie("Гадкий Я", 2010);
        store.addMovie("Муму", 1959);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/3"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(404, resp.statusCode(), "GET /movies/3 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Фильм не найден", errorResponse.getError());
    }

    @DisplayName("возвращает ошибку, если id не число")
    @Test
    public void testGetMovies_IdNotNumber() throws IOException, InterruptedException {
        store.addMovie("Гадкий Я", 2010);
        store.addMovie("Муму", 1959);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/num"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(400, resp.statusCode(), "GET /movies/num должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Введён некорректный ID", errorResponse.getError());
    }

}
