package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {

    private final Map<Long, Movie> movies;
    private long idCount;

    public MoviesStore() {
        this.movies = new HashMap<>();
        this.idCount = 0;
    }

    public List<Movie> getMovies() {
        return new ArrayList<>(movies.values());
    }

    public Movie addMovie(String title, int year) {
        idCount++;
        Movie movie = new Movie(idCount, title, year);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public Optional<Movie> getMovie(long id) {
        return Optional.ofNullable(movies.get(id));
    }

    public Movie deleteMovie(long id) {
        return movies.remove(id);
    }

    public List<Movie> getMoviesByYear(int year) {
        List<Movie> moviesByYear = new ArrayList<>(movies.values());
        return moviesByYear.stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public void clear() {
        movies.clear();
        idCount = 0;
    }

}