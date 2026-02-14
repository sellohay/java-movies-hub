package ru.practicum.moviehub.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {

    public static final int MINIMUM_YEAR = 1888;
    public static final int MAXIMUM_YEAR = 2027;
    public static final int MAXIMUM_LENGTH = 100;
    public static final String MOVIES_PATH = "/movies";

    public MoviesHandler(MoviesStore moviesStore) {
        super(moviesStore);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        String query = ex.getRequestURI().getQuery();
        if (method.equalsIgnoreCase("GET")) {
            if (path.equalsIgnoreCase(MOVIES_PATH) && query == null) {
                handleGetMovies(ex);
                return;
            }
            if (parts.length == 3 && parts[1].equalsIgnoreCase("movies")) {
                handleGetMoviesById(ex);
                return;
            }
            if (parts.length == 2 && parts[1].contains("movies") && query != null && query.contains("year=")) {
                handleGetMoviesByYear(ex);
                return;
            }
        }
        if (method.equalsIgnoreCase("POST") && path.equalsIgnoreCase(MOVIES_PATH)) {
            handlePostMovie(ex);
            return;
        }
        if (method.equalsIgnoreCase("DELETE") && parts.length == 3
                && parts[1].equalsIgnoreCase("movies")) {
            handleDeleteMovie(ex);
            return;
        }

        handleIncorrectMethod(ex);
    }

    private void handleIncorrectMethod(HttpExchange ex) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse("Некорректный метод");
        sendJson(ex, 405, gson.toJson(errorResponse));
    }

    private void handleGetMoviesByYear(HttpExchange ex) throws IOException {
        String yearStr = ex.getRequestURI().getQuery().split("=")[1];
        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            ErrorResponse errorResponse = new ErrorResponse("Введён некорректный год");
            sendJson(ex, 400, gson.toJson(errorResponse));
            return;
        }

        List<Movie> movies = moviesStore.getMoviesByYear(year);
        sendJson(ex, 200, gson.toJson(movies));
    }

    private void handleDeleteMovie(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        int movieId;
        try {
            movieId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            ErrorResponse errorResponse = new ErrorResponse("Введён некорректный ID");
            sendJson(ex, 400, gson.toJson(errorResponse));
            return;
        }

        Movie deletedMovie = moviesStore.deleteMovie(movieId);
        if (deletedMovie == null) {
            ErrorResponse errorResponse = new ErrorResponse("Фильм не найден");
            sendJson(ex, 404, gson.toJson(errorResponse));
            return;
        }
        sendNoContent(ex);
    }

    private void handleGetMoviesById(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        int movieId;
        try {
            movieId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            ErrorResponse errorResponse = new ErrorResponse("Введён некорректный ID");
            sendJson(ex, 400, gson.toJson(errorResponse));
            return;
        }

        Optional<Movie> movieOpt = moviesStore.getMovie(movieId);
        if (movieOpt.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse("Фильм не найден");
            sendJson(ex, 404, gson.toJson(errorResponse));
            return;
        }

        Movie movie = movieOpt.get();
        sendJson(ex, 200, gson.toJson(movie));
    }

    private void handlePostMovie(HttpExchange ex) throws IOException {

        Headers headers = ex.getRequestHeaders();
        if (!headers.get("Content-Type").contains("application/json; charset=UTF-8")) {
            ErrorResponse errorResponse = new ErrorResponse("Ошибка заголовков");
            errorResponse.addDetail("Неверное значение заголовка Content-Type");
            sendJson(ex, 415, gson.toJson(errorResponse));
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement element;
        try {
            element = JsonParser.parseString(body);
        } catch (JsonSyntaxException e) {
            ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации тела");
            errorResponse.addDetail("Некорректный формат JSON");
            sendJson(ex, 400, gson.toJson(errorResponse));
            return;
        }

        JsonObject movieJson = element.getAsJsonObject();

        Movie validatedMovie = validateMovie(ex, movieJson);
        if (validatedMovie != null) {
            sendJson(ex, 201, gson.toJson(validatedMovie));
        }
    }

    private Movie validateMovie(HttpExchange ex, JsonObject movieJson) throws IOException {
        ErrorResponse error = null;

        String title = movieJson.get("title").getAsString();
        if (title == null || title.isEmpty()) {
            error = addValidationError(error, "Название не должно быть пустым");
        } else
        if (title.length() > MAXIMUM_LENGTH) {
            error = addValidationError(error, "Название не должно превышать 100 символов в длину");
        }
        int year;
        try {
            year = movieJson.get("year").getAsInt();
        } catch (NumberFormatException e) {
            error = addValidationError(error, "Введен некорректный формат года (должно быть число)");
            sendJson(ex, 422, gson.toJson(error));
            return null;
        }

        if (year < MINIMUM_YEAR || year > MAXIMUM_YEAR) {
            error = addValidationError(error, "Введен некорректный год (должен быть между "
                    + MINIMUM_YEAR + " и " + MAXIMUM_YEAR + ")");
        }

        if (error != null) {
            sendJson(ex, 422, gson.toJson(error));
            return null;
        }
        return moviesStore.addMovie(title, year);
    }

    private ErrorResponse addValidationError(ErrorResponse error, String detail) {
        if (error == null) {
            error = new ErrorResponse("Ошибка валидации");
        }
        error.addDetail(detail);
        return error;
    }

    private void handleGetMovies(HttpExchange ex) throws IOException {
        List<Movie> movies = this.moviesStore.getMovies();
        sendJson(ex, 200, gson.toJson(movies));
    }
}
