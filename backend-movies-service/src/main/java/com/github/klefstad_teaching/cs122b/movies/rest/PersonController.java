package com.github.klefstad_teaching.cs122b.movies.rest;

import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.movies.model.Person;
import com.github.klefstad_teaching.cs122b.movies.model.PersonByIdResponse;
import com.github.klefstad_teaching.cs122b.movies.model.PersonResponseModel;
import com.github.klefstad_teaching.cs122b.movies.repo.MovieRepo;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PersonController
{
    private final MovieRepo repo;

    @Autowired
    public PersonController(MovieRepo repo)
    {
        this.repo = repo;
    }
    // /person/{personId}
    @GetMapping("/person/{personId}")
    public ResponseEntity<PersonByIdResponse> personSearch(@AuthenticationPrincipal SignedJWT user,
                                                          @PathVariable Long personId
                                                           )
    {
        PersonByIdResponse response = new PersonByIdResponse();
        Person resultPerson = repo.searchPersonByPersonId(personId);

        if(resultPerson == null){
            response.setResult(MoviesResults.NO_PERSON_WITH_ID_FOUND);
            return response.toResponse();
        }
        response.setPerson(resultPerson);
        response.setResult(MoviesResults.PERSON_WITH_ID_FOUND);
        return response.toResponse();
    }
}
