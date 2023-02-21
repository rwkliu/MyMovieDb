package com.github.klefstad_teaching.cs122b.movies.model;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import org.apache.catalina.connector.Response;

import java.util.List;

public class PersonResponseModel extends ResponseModel<PersonResponseModel> {
    private List<Person> persons;

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
}
