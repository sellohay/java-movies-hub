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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.http.MoviesHandler.MAXIMUM_YEAR;
import static ru.practicum.moviehub.http.MoviesHandler.MINIMUM_YEAR;

public class MoviesPostTest extends MoviesApiTest {


    @DisplayName("добавляет фильм при корректных данных")
    @Test
    public void testPostMoviesCorrect() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "Интерстеллар",
                    "year": 2014
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(201, resp.statusCode(), "POST /movies должен возвращать 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Movie movie = gson.fromJson(resp.body(), Movie.class);
        assertEquals(1, movie.getId() );
        assertEquals("Интерстеллар", movie.getTitle() );
        assertEquals(2014, movie.getYear() );

    }

    @DisplayName("возвращает ошибку при пустом title")
    @Test
    public void testPostMovies_EmptyTitle() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "",
                    "year": 2000
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(422, resp.statusCode(), "POST /movies должен возвращать 422");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Название не должно быть пустым"));
    }

    @DisplayName("возвращает ошибку при слишком длинном title (> 100 символов)")
    @Test
    public void testPostMovies_TitleTooLong() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "очень длинное название фильма ааааааааааааааааааааааааааааааааааааааааааааа
                """ + """
                    аааааааааааааааааааааааааа",
                    "year": 2000
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(422, resp.statusCode(), "POST /movies должен возвращать 422");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Название не должно превышать 100 символов в длину"));
    }

    @DisplayName("возвращает ошибку при неверном year (<1888)")
    @Test
    public void testPostMovies_YearIncorrect1() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "Очень древний фильм",
                    "year": 1869
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(422, resp.statusCode(), "POST /movies должен возвращать 422");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Введен некорректный год (должен быть между "
                + MINIMUM_YEAR + " и " + MAXIMUM_YEAR + ")"));
    }

    @DisplayName("возвращает ошибку при неверном year (>2027)")
    @Test
    public void testPostMovies_YearIncorrect2() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "Фильм из будущего",
                    "year": 2030
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(422, resp.statusCode(), "POST /movies должен возвращать 422");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Введен некорректный год (должен быть между "
                + MINIMUM_YEAR + " и " + MAXIMUM_YEAR + ")"));
    }

    @DisplayName("возвращает ошибку при неправильном Content-Type")
    @Test
    public void testPostMovies_ContentTypeIncorrect() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "Интерстеллар",
                    "year": 2014
                }
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/html; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(415, resp.statusCode(), "POST /movies должен возвращать 415");
        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка заголовков", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Неверное значение заголовка Content-Type"));
    }

    @DisplayName("возвращает ошибку при некорректном JSON")
    @Test
    public void testPostMovies_IncorrectJson() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "title": "Интерстеллар",
                    "year": 2014
                """;
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(400, resp.statusCode(), "POST /movies должен возвращать 400");
        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Ошибка валидации тела", errorResponse.getError());
        assertTrue(errorResponse.getDetails().contains("Некорректный формат JSON"));
    }
}
