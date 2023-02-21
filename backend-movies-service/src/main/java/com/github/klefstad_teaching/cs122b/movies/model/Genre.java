package com.github.klefstad_teaching.cs122b.movies.model;

public class Genre {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public Genre Id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Genre setName(String name) {
        this.name = name;
        return this;
    }
}
