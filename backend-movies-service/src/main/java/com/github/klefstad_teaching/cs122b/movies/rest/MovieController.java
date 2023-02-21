package com.github.klefstad_teaching.cs122b.movies.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.movies.model.*;
import com.github.klefstad_teaching.cs122b.movies.repo.MovieRepo;
import com.github.klefstad_teaching.cs122b.movies.util.Validate;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class MovieController
{
    private final MovieRepo repo;
    private final Validate validate;

    @Autowired
    public MovieController(MovieRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }
    @GetMapping("/movie/search")
    public ResponseEntity<SearchResponseModel> movieSearch(@AuthenticationPrincipal SignedJWT user,
                                                           @RequestParam Optional<String> title,
                                                           @RequestParam Optional<Integer> year,
                                                           @RequestParam Optional<String> director,
                                                           @RequestParam Optional<String> genre,
                                                           @RequestParam Optional<Integer> limit,
                                                           @RequestParam Optional<Integer> page,
                                                           @RequestParam Optional<String> orderBy,
                                                           @RequestParam Optional<String> direction
    ) throws ParseException {
        //return a list of movies that match the search parameters

        //System.out.println("Everything looks good");

        if(limit.isPresent() && limit.get()!=10 && limit.get()!=25 && limit.get()!=50 && limit.get()!=100){
            throw new ResultError(MoviesResults.INVALID_LIMIT);
        }
        if(page.isPresent() && page.get()<=0){
            throw new ResultError(MoviesResults.INVALID_PAGE);
        }
        if(direction.isPresent()){
            System.out.println(direction.get());
            if(!direction.get().equals("desc") && !direction.get().equals("DESC") && !direction.get().equals("ASC") && !direction.get().equals("asc" )){
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }
        if(orderBy.isPresent()){
            if(!orderBy.get().equals("title") && !orderBy.get().equals("rating") && !orderBy.get().equals("year")){
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }


        List<String> roles = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        SearchResponseModel response = new SearchResponseModel();
        List<Movie> resultMovies = repo.search(title, year, director, genre, limit, page, orderBy, direction, roles);
        if(resultMovies == null || resultMovies.isEmpty()){
            response.setResult(MoviesResults.NO_MOVIES_FOUND_WITHIN_SEARCH);
            return response.toResponse();
        }
        response.setMovies(resultMovies);
        response.setResult(MoviesResults.MOVIES_FOUND_WITHIN_SEARCH);
        System.out.println("The result is");
        for(Movie m: resultMovies){
            System.out.println(", " + m.getTitle());
        }

        return response.toResponse();
    }

    @GetMapping("/movie/search/person/{personId}")
    public ResponseEntity<SearchResponseModel> movieSearch(@AuthenticationPrincipal SignedJWT user,
                                                           @RequestParam Optional<Integer> limit,
                                                           @RequestParam Optional<Integer> page,
                                                           @RequestParam Optional<String> orderBy,
                                                           @RequestParam Optional<String> direction,
                                                           @PathVariable Long personId
    ) throws ParseException {
        if(limit.isPresent() && limit.get()!=10 && limit.get()!=25 && limit.get()!=50 && limit.get()!=100){
            throw new ResultError(MoviesResults.INVALID_LIMIT);
        }
        if(page.isPresent() && page.get()<=0){
            throw new ResultError(MoviesResults.INVALID_PAGE);
        }
        if(direction.isPresent()){
            if(!direction.get().equals("desc") && !direction.get().equals("DESC") && !direction.get().equals("ASC") && !direction.get().equals("asc" )){
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }
        if(orderBy.isPresent()){
            if(!orderBy.get().equals("title") && !orderBy.get().equals("rating") && !orderBy.get().equals("year")){
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }
        System.out.println("person id is "+personId);
        SearchResponseModel response = new SearchResponseModel();
        List<String> roles = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        List<Movie> movies = repo.searchByPersonId(limit, page, orderBy, direction, personId, roles);
        if(movies.size()==0){
            response.setResult(MoviesResults.NO_MOVIES_WITH_PERSON_ID_FOUND);
            return response.toResponse();
        }
        response.setMovies(movies);
        response.setResult(MoviesResults.MOVIES_WITH_PERSON_ID_FOUND);
        return response.toResponse();
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<SearchByMovieIdResponse> searchByMovieId(@AuthenticationPrincipal SignedJWT user,
                                                                   @PathVariable Long movieId) throws ParseException
    {
        List<String> roles = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        SearchByMovieIdResponse response = new SearchByMovieIdResponse();
        response = repo.searchByMovieIdDb(movieId, roles);
        //response.setResult(MoviesResults.MOVIES_WITH_PERSON_ID_FOUND);
        return response.toResponse();

    }


    @GetMapping("/person/search")
    public ResponseEntity<PersonResponseModel> movieSearch(@AuthenticationPrincipal SignedJWT user,
                                                           @RequestParam Optional<String> name,
                                                           @RequestParam Optional<String> birthday,
                                                           @RequestParam Optional<String> movieTitle,
                                                           @RequestParam Optional<Integer> limit,
                                                           @RequestParam Optional<Integer> page,
                                                           @RequestParam Optional<String> orderBy,
                                                           @RequestParam Optional<String> direction
    ) throws ParseException {
        System.out.println(" /person/search is here");

        if (limit.isPresent() && limit.get() != 10 && limit.get() != 25 && limit.get() != 50 && limit.get() != 100) {
            throw new ResultError(MoviesResults.INVALID_LIMIT);
        }
        if (page.isPresent() && page.get() <= 0) {
            throw new ResultError(MoviesResults.INVALID_PAGE);
        }
        if (direction.isPresent()) {
            if (!direction.get().equals("desc") && !direction.get().equals("DESC") && !direction.get().equals("ASC") && !direction.get().equals("asc")) {
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }
        if (orderBy.isPresent()) {
            if (!orderBy.get().equals("name") && !orderBy.get().equals("popularity") && !orderBy.get().equals("birthday")) {
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }


        List<String> roles = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        System.out.println(roles);
        PersonResponseModel response = new PersonResponseModel();
        List<Person> resultPersons = repo.searchPerson(name, birthday, movieTitle, limit, page, orderBy, direction, roles);
        if (resultPersons == null || resultPersons.isEmpty()) {
            response.setResult(MoviesResults.NO_PERSONS_FOUND_WITHIN_SEARCH);
            return response.toResponse();
        }
        response.setPersons(resultPersons);
        response.setResult(MoviesResults.PERSONS_FOUND_WITHIN_SEARCH);
        return response.toResponse();
    }



}
