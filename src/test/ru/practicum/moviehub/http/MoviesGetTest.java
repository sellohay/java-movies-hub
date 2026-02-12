package ru.practicum.moviehub.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesGetTest extends MoviesApiTest {


    @DisplayName("возвращает пустой список, если нет фильмов")
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @DisplayName("возвращает список с ранее добавленными фильмами")
    @Test
    void testGetMoviesWithMovies() throws IOException, InterruptedException {

        store.addMovie("Гадкий Я", 2010);
        store.addMovie("Муму", 1959);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> movies = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, movies.size());
        Movie respMovie = movies.getFirst();
        assertEquals(1, respMovie.getId());
        assertEquals("Гадкий Я", respMovie.getTitle());
        assertEquals(2010, respMovie.getYear());

        respMovie = movies.getLast();
        assertEquals(2, respMovie.getId());
        assertEquals("Муму", respMovie.getTitle());
        assertEquals(1959, respMovie.getYear());

    }
}
