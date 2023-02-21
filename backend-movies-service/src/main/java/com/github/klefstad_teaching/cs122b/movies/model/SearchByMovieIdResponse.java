package com.github.klefstad_teaching.cs122b.movies.model;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;

import java.util.List;

public class SearchByMovieIdResponse extends ResponseModel<SearchByMovieIdResponse> {
    private MovieDetail movie;
    private List<Genre> genres;
    private List<Person2> persons;

    public MovieDetail getMovie() {
        return movie;
    }

    public SearchByMovieIdResponse setMovie(MovieDetail movie) {
        this.movie = movie;
        return this;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public SearchByMovieIdResponse setGenres(List<Genre> genres) {
        this.genres = genres;
        return this;
    }

    public List<Person2> getPersons() {
        return persons;
    }

    public SearchByMovieIdResponse setPersons(List<Person2> persons) {
        this.persons = persons;
        return this;
    }
}
