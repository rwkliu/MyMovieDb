package com.github.klefstad_teaching.cs122b.movies.model;
import java.math.BigInteger;

public class Movie {
    private Long id;
    private String title;
    private Integer year;
    private String director;
    private Double rating;
    private String backdropPath;
    private String posterPath;
    private boolean hidden;

    public Long getId() {
        return id;
    }

    public Movie setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Movie setTitle(String title) {
        this.title = title;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public Movie setYear(Integer year) {
        this.year = year;
        return this;
    }

    public String getDirector() {
        return director;
    }

    public Movie setDirector(String director) {
        this.director = director;
        return this;
    }

    public Double getRating() {
        return rating;
    }

    public Movie setRating(Double rating) {
        this.rating = rating;
        return this;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Movie setBackdrop_path(String backdropPath) {
        this.backdropPath = backdropPath;
        return this;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Movie setPoster_path(String posterPath) {
        this.posterPath = posterPath;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Movie setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }
}
