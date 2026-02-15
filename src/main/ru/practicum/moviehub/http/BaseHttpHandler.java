package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

abstract class BaseHttpHandler implements HttpHandler {

    protected final MoviesStore moviesStore;
    protected final Gson gson;
    protected static final String CT_JSON =  "application/json; charset=UTF-8";

    public BaseHttpHandler(final MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        this.gson = new Gson();
    }

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }
}