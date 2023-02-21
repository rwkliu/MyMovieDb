package com.github.klefstad_teaching.cs122b.movies.model;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;

import java.util.List;

public class SearchResponseModel extends ResponseModel<com.github.klefstad_teaching.cs122b.movies.model.SearchResponseModel> {
        private List<Movie> movies;

        public List<Movie> getMovies() {
            return movies;
        }

        public void setMovies(List<Movie> movies) {
            this.movies = movies;
        }


}
