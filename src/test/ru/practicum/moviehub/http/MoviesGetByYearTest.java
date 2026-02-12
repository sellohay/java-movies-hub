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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesGetByYearTest extends MoviesGetTest {

    @DisplayName("возвращает фильмы указанного года")
    @Test
    public void testGetMovieByYear() throws IOException, InterruptedException {
        store.addMovie("Фильм 1", 2008);
        store.addMovie("Фильм 2", 2010);
        store.addMovie("Фильм 3", 2003);
        store.addMovie("Фильм 4", 2010);
        store.addMovie("Фильм 5", 2006);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=2010"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies?year=2010 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> movies = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, movies.size());
        Movie movie = movies.getFirst();
        assertEquals(2, movie.getId());
        assertEquals("Фильм 2", movie.getTitle());
        assertEquals(2010, movie.getYear());
        movie = movies.getLast();
        assertEquals(4, movie.getId());
        assertEquals("Фильм 4", movie.getTitle());
        assertEquals(2010, movie.getYear());

    }

    @DisplayName("возвращает пустой список, если фильмов с таким годом нет")
    @Test
    public void testGetMovieByYear_Empty() throws IOException, InterruptedException {
        store.addMovie("Фильм 1", 2008);
        store.addMovie("Фильм 2", 2010);
        store.addMovie("Фильм 3", 2003);
        store.addMovie("Фильм 4", 2010);
        store.addMovie("Фильм 5", 2006);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=2007"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies?year=2007 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @DisplayName("возвращает ошибку, если параметр year не число")
    @Test
    public void testGetMovieByYear_InvalidYear() throws IOException, InterruptedException {
        store.addMovie("Фильм 1", 2008);
        store.addMovie("Фильм 2", 2010);
        store.addMovie("Фильм 3", 2003);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=num"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(400, resp.statusCode(), "GET /movies?year=num должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Введён некорректный год", errorResponse.getError());
    }

}
