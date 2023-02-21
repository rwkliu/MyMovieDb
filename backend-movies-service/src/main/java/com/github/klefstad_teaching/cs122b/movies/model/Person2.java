package com.github.klefstad_teaching.cs122b.movies.model;

public class Person2 {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public Person2 setId(Long personId) {
        this.id = personId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Person2 setName(String name) {
        this.name = name;
        return this;
    }
}
