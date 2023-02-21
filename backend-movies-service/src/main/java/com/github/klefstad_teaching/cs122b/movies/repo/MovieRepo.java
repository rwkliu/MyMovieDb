package com.github.klefstad_teaching.cs122b.movies.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.movies.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MovieRepo
{
    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate template;

    @Autowired
    public MovieRepo(ObjectMapper objectMapper, NamedParameterJdbcTemplate template)
    {
        this.objectMapper = objectMapper;
        this.template = template;
    }
    public ObjectMapper getObjectMapper(){return objectMapper;}
    public NamedParameterJdbcTemplate getTemplate(){return template;}

    //public List<Movie> stringTest(String select)

    private final static String MOVIE_WITH_GENRE =
            "SELECT DISTINCT m.id, m.title, m.year, m.director_id, m.rating, m.num_votes, m.revenue, m.overview, m.backdrop_path, m.poster_path, m.hidden, p.name " +
                    "FROM movies.movie m " +
                    "    JOIN movies.movie_genre mg ON m.id = mg.movie_id " +
                    "    JOIN movies.genre g ON mg.genre_id = g.id " +
                    "    JOIN movies.person p ON m.director_id = p.id ";
    private final static String MOVIE_WITH_NO_GENRE =
            "SELECT DISTINCT m.id, m.title, m.year, m.director_id, m.rating, m.num_votes, m.revenue, m.overview, m.backdrop_path, m.poster_path, m.hidden, p.name " +
                    "FROM movies.movie m " +
                    "    JOIN movies.person p ON m.director_id = p.id ";

    public boolean isAdminOrEmployee(List<String> roles){
        for(String role: roles){
            if(role.equals("ADMIN") || role.equals("EMPLOYEE")){
                return true;
            }
        }
        return false;
    }
    public List<Movie> search(@RequestParam Optional<String> title,
                              @RequestParam Optional<Integer> year,
                              @RequestParam Optional<String> director,
                              @RequestParam Optional<String> genre,
                              @RequestParam Optional<Integer> limit,
                              @RequestParam Optional<Integer> page,
                              @RequestParam Optional<String> orderBy,
                              @RequestParam Optional<String> direction,
                              List<String> roles) {
        StringBuilder sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        if (genre.isPresent()) {
            sql = new StringBuilder(MOVIE_WITH_GENRE);
            sql.append(" WHERE g.name LIKE :genre ");
            String wildcardSearch = '%' + genre.get() + '%';

            source.addValue("genre", wildcardSearch, Types.VARCHAR);
            whereAdded = true;
        } else {
            sql = new StringBuilder(MOVIE_WITH_NO_GENRE);
        }
        if(!isAdminOrEmployee(roles)){
            if (whereAdded) {
                sql.append(" AND (m.hidden = :hidden) ");
            } else {
                sql.append(" WHERE (m.hidden = :hidden)");
                whereAdded = true;
            }

            source.addValue("hidden", false, Types.BOOLEAN);
            //source.addValue("isAdminOrEmployee", true, Types.BOOLEAN);
        }
        if (title.isPresent()) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }
            sql.append(" m.title LIKE :title ");
            String wildcardSearch = '%' + title.get() + '%';
            source.addValue("title", wildcardSearch, Types.VARCHAR);
        }
        if (year.isPresent()) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }
            sql.append(" m.year = :year ");
            source.addValue("year", year.get(), Types.VARCHAR);
        }

        //director
        //orderBy
        //direction
        if (director.isPresent()) {
            //sql.append("JOIN movies.person p ON m.director_id = p.id WHERE ");
            //sql
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }
            sql.append(" p.name LIKE :director ");
            String wildcardSearch = '%' + director.get() + '%';

            source.addValue("director", wildcardSearch, Types.VARCHAR);
            whereAdded = true;
        }


        sql.append(" ORDER BY ");
        if (orderBy.isPresent() && orderBy!=null) {
            if (orderBy.get().equals("title")) {
                sql.append(" TITLE ");
            }
            if (orderBy.get().equals("rating")) {
                sql.append(" RATING ");
            }
            if (orderBy.get().equals("year")) {
                sql.append(" YEAR ");
            }

        } else {
            sql.append(" TITLE ");
        }

        if (direction.isPresent()) {

            if (direction.get().equals("desc")||direction.get().equals("DESC")) {
                sql.append(" DESC");
            }
            if (direction.get().equals("asc")||direction.get().equals("ASC")) {
                sql.append(" ASC");
            }
        } else {
            sql.append(" ASC");
        }

        sql.append(", m.id ASC ");

        if(limit.isPresent()){
            sql.append(" LIMIT :limit ");
            source.addValue("limit", limit.get(), Types.INTEGER);
            if (page.isPresent()) {
                Integer offset = (page.get() - 1) * limit.get();
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }
        } else{
            sql.append(" LIMIT :limit ");
            source.addValue("limit", 10, Types.INTEGER);
            if (page.isPresent()) {
                Integer offset = (page.get() - 1) *10;
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }
        }


        System.out.println("SQL query is " + sql);
        List<Movie> movies = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Movie()
                                .setId(rs.getLong("id"))
                                .setTitle(rs.getString("title"))
                                .setYear(rs.getInt("year"))
                                .setDirector(rs.getString("name"))
                                .setRating(rs.getDouble("rating"))
                                .setBackdrop_path(rs.getString("backdrop_path"))
                                .setPoster_path(rs.getString("poster_path"))
                                .setHidden(rs.getBoolean("hidden"))

        );

        return movies;
    }



    public List<Movie> searchByPersonId(  @RequestParam Optional<Integer> limit,
                                          @RequestParam Optional<Integer> page,
                                          @RequestParam Optional<String> orderBy,
                                          @RequestParam Optional<String> direction,
                                          @PathVariable Long personId, List<String> roles)
    {
        String SQL = "SELECT m.id, m.title, m.year,  p.name,  m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                "FROM movies.movie m " +
                "JOIN movies.person p ON m.director_id = p.id " +
                "JOIN movies.movie_person mp ON m.id = mp.movie_id " +
                "WHERE mp.person_id = :person_id ";
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource source = new MapSqlParameterSource();
        sql.append(SQL);
        if(!isAdminOrEmployee(roles)){
            sql.append(" AND hidden = 0 ");
        }
        source.addValue("person_id", personId, Types.INTEGER);
        sql.append(" ORDER BY ");
        if (orderBy.isPresent()) {
            if (orderBy.get().equals("title")) {
                sql.append(" TITLE ");
            }
            if (orderBy.get().equals("rating")) {
                sql.append(" RATING ");
            }
            if (orderBy.get().equals("year")) {
                sql.append(" YEAR ");
            }

        } else {
            sql.append(" TITLE ");
        }

        if (direction.isPresent()) {

            if (direction.get().equals("desc")||direction.get().equals("DESC")) {
                sql.append(" DESC");
            }
            if (direction.get().equals("asc")||direction.get().equals("ASC")) {
                sql.append(" ASC");
            }
        } else {
            sql.append(" ASC");
        }

        sql.append(", m.id ASC ");

        if(limit.isPresent()){
            sql.append(" LIMIT :limit ");
            source.addValue("limit", limit.get(), Types.INTEGER);
            if (page.isPresent()) {
                Integer offset = (page.get() - 1) * limit.get();
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }
        } else{
            sql.append(" LIMIT :limit ");
            source.addValue("limit", 10, Types.INTEGER);
            if (page.isPresent()) {
                Integer offset = (page.get() - 1) *10;
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }
        }
        List<Movie> movies = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Movie()
                                .setId(rs.getLong("id"))
                                .setTitle(rs.getString("title"))
                                .setYear(rs.getInt("year"))
                                .setDirector(rs.getString("name"))
                                .setRating(rs.getDouble("rating"))
                                .setBackdrop_path(rs.getString("backdrop_path"))
                                .setPoster_path(rs.getString("poster_path"))
                                .setHidden(rs.getBoolean("hidden"))

        );

        return movies;

    }
    private final static String PERSONSQL =
            "SELECT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path " +
                    "FROM movies.person p ";
    public Person searchPersonByPersonId(@PathVariable Long personId){
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource source = new MapSqlParameterSource();
        sql.append(PERSONSQL);
        sql.append("WHERE p.id = :id ");
        source.addValue("id", personId, Types.INTEGER);
        System.out.println("SQL query is " + sql);

        try {
            Person personList = this.template.queryForObject(
                    sql.toString(),
                    source,
                    (rs, rowNum) ->
                            new Person()
                                    .setId(rs.getLong("id"))
                                    .setName(rs.getString("name"))
                                    .setBirthday(rs.getString("birthday"))
                                    .setBiography(rs.getString("biography"))
                                    .setBirthplace(rs.getString("birthplace"))
                                    .setPopularity(rs.getDouble("popularity"))
                                    .setProfilePath(rs.getString("profile_path"))

            );

            return personList;
        }
        catch (Exception e){
            return null;
        }

    }

    private final static String PERSONBYNAME =
            "SELECT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path " +
                    "FROM movies.person p ";

    private final static String PERSONBYTITLE =
            "JOIN movies.movie_person mp ON p.id = mp.person_id " +
                    "JOIN movies.movie m ON mp.movie_id = m.id ";
    public List<Person> searchPerson( Optional<String> name,
                               Optional<String> birthday,
                               Optional<String> movieTitle,
                               Optional<Integer> limit,
                               Optional<Integer> page,
                               Optional<String> orderBy,
                               Optional<String> direction,
                                      List<String> roles)
    {
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource source = new MapSqlParameterSource();
        sql.append(PERSONBYNAME);
        //by name
        boolean whereAdded = false;
        if (name.isPresent()) {
            //sql.append("JOIN movies.person p ON m.director_id = p.id WHERE ");
            //sql
                sql.append(" WHERE ");
                whereAdded = true;
            sql.append(" p.name LIKE :name ");
            String wildcardSearch = '%' + name.get()+ '%';

            source.addValue("name", wildcardSearch, Types.VARCHAR);

        }
        //movie title wild card
        if (movieTitle.isPresent()) {
            //sql.append("JOIN movies.person p ON m.director_id = p.id WHERE ");
            sql.append(PERSONBYTITLE);
            sql.append(" WHERE ");
            whereAdded = true;
            sql.append(" m.title LIKE :title ");
            String wildcardSearch = '%' + movieTitle.get()+ '%';

            source.addValue("title", wildcardSearch, Types.VARCHAR);

        }
        if (birthday.isPresent()) {
            //sql.append("JOIN movies.person p ON m.director_id = p.id WHERE ");
            sql.append(" WHERE ");
            whereAdded = true;
            sql.append(" p.birthday=:birthday ");

            source.addValue("birthday", birthday.get(), Types.VARCHAR);

        }

        sql.append(" ORDER BY ");
        if (orderBy.isPresent()) {
            if (orderBy.get().equals("name")) {
                sql.append(" NAME ");
            }
            if (orderBy.get().equals("popularity")) {
                sql.append(" POPULARITY ");
            }
            if (orderBy.get().equals("birthday")) {
                sql.append(" BIRTHDAY ");
            }

        } else {
            sql.append(" NAME ");
        }

        if (direction.isPresent()) {

            if (direction.get().equals("desc")||direction.get().equals("DESC")) {
                sql.append(" DESC");
            }
            if (direction.get().equals("asc")||direction.get().equals("ASC")) {
                sql.append(" ASC");
            }
        } else {
            sql.append(" ASC");
        }

        sql.append(", p.id ASC ");

        if(limit.isPresent()){
            sql.append(" LIMIT :limit ");
            source.addValue("limit", limit.get(), Types.INTEGER);

            if (page.isPresent()) {
                Integer offset = (page.get() - 1) * limit.get();
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }

        } else{
            sql.append(" LIMIT :limit ");
            source.addValue("limit", 10, Types.INTEGER);
            if (page.isPresent()) {
                Integer offset = (page.get() - 1) * 10;
                sql.append(" OFFSET :offset ");
                source.addValue("offset", offset, Types.INTEGER);
            } else {
                sql.append(" OFFSET :offset ");
                source.addValue("offset", 0, Types.INTEGER);
            }
        }

        System.out.println("SQL query is " + sql);

        List<Person> personList = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Person()
                                .setId(rs.getLong("id"))
                                .setName(rs.getString("name"))
                                .setBirthday(rs.getString("birthday"))
                                .setBiography(rs.getString("biography"))
                                .setBirthplace(rs.getString("birthplace"))
                                .setPopularity(rs.getDouble("popularity"))
                                .setProfilePath(rs.getString("profile_path"))

        );

        return personList;
    }

    public SearchByMovieIdResponse searchByMovieIdDb(Long movieId, List<String> roles ) {
        String SQL = "SELECT DISTINCT m.id, m.title, m.year, p.name, m.rating, m.num_votes, m.budget, m.revenue, m.overview, m.backdrop_path, m.poster_path, m.hidden, " +
                "(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', g.id, 'name', g.name)) " +
                "FROM (SELECT DISTINCT g.id, g.name " +
                "      FROM movies.genre g " +
                "      JOIN movies.movie_genre mg ON g.id = mg.genre_id " +
                "      JOIN movies.movie_genre ON m.id = mg.movie_id " +
                "      WHERE m.id = :movieId " +
                "      ORDER BY g.name) as g) AS genres, " +
                "(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', p.id, 'name', p.name)) " +
                "FROM (SELECT DISTINCT p.id, p.name, p.popularity, mp.person_id " +
                "      FROM movies.person p JOIN movies.movie_person mp ON p.id = mp.person_id " +
                "      WHERE mp.movie_id = :movieId " +
                "      ORDER BY p.popularity DESC, mp.person_id ASC) as p) AS persons " +
                "FROM movies.movie m " +
                "JOIN movies.person p ON p.id = m.director_id " +
                "WHERE m.id = :movieId ";
        if(!isAdminOrEmployee(roles)){
            SQL += " AND hidden = 0 ";
        }
        try {
            SearchByMovieIdResponse response =
                    this.template.queryForObject(
                            SQL,
                            new MapSqlParameterSource().addValue("movieId", movieId, Types.INTEGER),
                            this::methodInsteadOfLambdaForMapping
                    );

            response.setResult(MoviesResults.MOVIE_WITH_ID_FOUND);
            return response;
        } catch(EmptyResultDataAccessException e){
            throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
        }
    }
    private SearchByMovieIdResponse methodInsteadOfLambdaForMapping(ResultSet rs, int rowNumber)
            throws SQLException
    {
        List<Genre> genres = null;
        List<Person2> persons = null;
        Person2[] personArray = new Person2[0];

        try {
            String jsonGenreString = rs.getString("genres");
            String jsonPersonString = rs.getString("persons");
            Genre[] genreArray = objectMapper.readValue(jsonGenreString,Genre[].class);
            if(jsonPersonString!=null){
                personArray = objectMapper.readValue(jsonPersonString, Person2[].class);
            }

            // This just helps convert from an Object Array to a List<>
            genres = Arrays.stream(genreArray).collect(Collectors.toList());
            persons = Arrays.stream(personArray).collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            return new SearchByMovieIdResponse().setResult(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
        }

        MovieDetail movieDetail =
                new MovieDetail()
                        .setId(rs.getLong("id"))
                        .setTitle(rs.getString("title"))
                        .setYear(rs.getInt("year"))
                        .setDirector(rs.getString("name"))
                        .setRating(rs.getDouble("rating"))
                        .setNumVotes(rs.getInt("num_votes"))
                        .setBudget(rs.getLong("budget"))
                        .setRevenue(rs.getLong("revenue"))
                        .setOverview(rs.getString("overview"))
                        .setBackdropPath(rs.getString("backdrop_path"))
                        .setPosterPath(rs.getString("poster_path"))
                        .setHidden(rs.getBoolean("hidden"));

        return new SearchByMovieIdResponse()
                .setMovie(movieDetail)
                .setPersons(persons)
                .setGenres(genres);
    }



}
