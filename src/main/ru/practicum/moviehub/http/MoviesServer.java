package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {

    private final MoviesStore moviesStore;
    private final int port;
    private final HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) {
        this.moviesStore = moviesStore;
        this.port = port;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/movies", new MoviesHandler(moviesStore));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + port);
    }

    public void stop() {
        server.stop(1);
        System.out.println("Сервер остановлен");
    }
}