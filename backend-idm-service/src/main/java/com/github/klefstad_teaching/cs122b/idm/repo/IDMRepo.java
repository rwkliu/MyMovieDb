package com.github.klefstad_teaching.cs122b.idm.repo;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;

@Component
public class IDMRepo
{

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public IDMRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public void insertUser(User user)
    {
        try {
            int rowsUpdated = this.template.update(
                    "INSERT INTO idm.user (email, user_status_id, salt, hashed_password )" +
                            "VALUES (:email, :user_status_id, :salt, :hashed_password);",
                    new MapSqlParameterSource()
                            .addValue("email", user.getEmail(), Types.VARCHAR)
                            //check here
                            .addValue("user_status_id", user.getUserStatus().id(), Types.INTEGER)
                            .addValue("salt", user.getSalt(), Types.VARCHAR)
                            .addValue("hashed_password", user.getHashedPassword(), Types.VARCHAR)
            );
        } catch(Exception e){

            throw new ResultError(IDMResults.USER_ALREADY_EXISTS);
    }

    }
    public User getUser(String email) {
        try {
            //User users = this.template.queryForObject(

            User user = this.template.queryForObject(
                    "SELECT id, email, user_status_id, salt, hashed_password " +
                            "FROM idm.user WHERE email=:email;",
                    new MapSqlParameterSource()
                            .addValue("email", email, Types.VARCHAR),
                    (rs, rowNum) ->
                            new User()
                                    .setId(rs.getInt("id"))
                                    .setEmail(rs.getString("email"))
                                    .setUserStatus(UserStatus.fromId(rs.getInt("user_status_id")))
                                    .setSalt(rs.getString("salt"))
                                    .setHashedPassword(rs.getString("hashed_password"))


            );
            return user;

        } catch (DataAccessException e) {
            throw new ResultError(IDMResults.USER_NOT_FOUND);

        }

    }
    public void insertRefreshToken(RefreshToken refreshToken){
        try {
            System.out.println(refreshToken.getToken()+" "+refreshToken.getUserId()+" "+refreshToken.getTokenStatus().id()+" "+refreshToken.getExpireTime());
            int rowsUpdated = this.template.update(
                    // private Integer     id;
                    //    private String      token;
                    //    private Integer     userId;
                    //    private TokenStatus tokenStatus;
                    //    private Instant     expireTime;
                    //    private Instant     maxLifeTime;
                    "INSERT INTO idm.refresh_token (token, user_id, token_status_id, expire_time, max_life_time )" +
                            "VALUES (:token, :user_id,:token_status_id, :expire_time, :max_life_time );",
                    new MapSqlParameterSource()
                            .addValue("token", refreshToken.getToken(), Types.VARCHAR)
                            .addValue("user_id", refreshToken.getUserId(), Types.INTEGER)
                            .addValue("token_status_id", refreshToken.getTokenStatus().id(), Types.INTEGER)
                            .addValue("expire_time", Timestamp.from(refreshToken.getExpireTime()), Types.TIMESTAMP)
                            .addValue("max_life_time",Timestamp.from(refreshToken.getMaxLifeTime()), Types.TIMESTAMP)
            );
        } catch(Exception e){
            System.out.println("going to throw an error");
            e.printStackTrace();
            throw new ResultError(IDMResults.USER_ALREADY_EXISTS);
        }
    }

    public RefreshToken getRefreshToken(String token)
    {
        try {
            RefreshToken refreshToken = this.template.queryForObject(
                    "SELECT id, token, user_id, token_status_id, expire_time,max_life_time " +
                            "FROM idm.refresh_token WHERE token=:token;",
                    new MapSqlParameterSource()
                            .addValue("token", token, Types.VARCHAR),
                    (rs, rowNum) ->
                            new RefreshToken()
                                    .setId(rs.getInt("id"))
                                    .setToken(rs.getString("token"))
                                    .setUserId(rs.getInt("user_id"))
                                    .setTokenStatus(TokenStatus.fromId(rs.getInt("token_status_id")))
                                    .setExpireTime(rs.getTimestamp("expire_time").toInstant())
                                    .setMaxLifeTime(rs.getTimestamp("max_life_time").toInstant())

            );
            return refreshToken;

        } catch (DataAccessException e) {
            System.out.println("There error 3");

            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);

        }
    }

    public void updateRefreshToken(String token){
        try {
            int rowsUpdated = this.template.update(

                    "UPDATE idm.refresh_token " +
                            "SET token_status_id = :token_status_id "+
                            "WHERE token=:token;",
                    new MapSqlParameterSource()
                            .addValue("token", token, Types.VARCHAR)//,
                    //(rs, rowNum) ->
                            //new RefreshToken()
                                    ////.setTokenStatus(TokenStatus.fromId(rs.getInt("token_status_id")))
            );
        } catch(Exception e){
            System.out.println("There error 4");

            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);
        }
    }
    public void updateRefreshTokenStatusExpire(RefreshToken rt)
    {
        try {
            String token = rt.getToken();
            int rowsUpdated = this.template.update(
                    "UPDATE idm.refresh_token " +
                            "SET token_status_id = :token_status_id "+
                            "WHERE token=:token;",
                    new MapSqlParameterSource()

                            .addValue("token_status_id", TokenStatus.EXPIRED.id(), Types.INTEGER)
                            .addValue("token",rt.getToken(), Types.VARCHAR)
            );
        } catch(Exception e){
            System.out.println("Thre error is here 5");
            e.printStackTrace();
            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);
        }

    }
    public void updateRefreshTokenStatusRevoke(RefreshToken rt)
    {
        try {
            String token = rt.getToken();
            int rowsUpdated = this.template.update(

                    "UPDATE idm.refresh_token " +
                            "SET token_status_id = :token_status_id "+
                            "WHERE token=:token;",
                    new MapSqlParameterSource()

                            .addValue("token_status_id", TokenStatus.REVOKED.id(), Types.INTEGER)
                            .addValue("token",rt.getToken(), Types.VARCHAR)

            );
        } catch(Exception e){
            System.out.println("There error 1");
            e.printStackTrace();
            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);
        }

    }
    //public void updateRefreshTokenExpireTime(RefreshToken token)
    public void updateRefreshTokenExpireTime(RefreshToken rt)
    {

        try {

            int rowsUpdated = this.template.update(

                    "UPDATE idm.refresh_token " +
                            "SET expire_time = :expire_time "+
                            "WHERE token = :token;",
                    new MapSqlParameterSource()

                            .addValue("expire_time", Timestamp.from(rt.getExpireTime()), Types.TIMESTAMP)
                            .addValue("token",rt.getToken(), Types.VARCHAR)

            );
        } catch(Exception e){
            e.printStackTrace();
            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);
        }

    }
    // User getUserFromRefreshToken(RefreshToken refreshToken)
    //    public RefreshToken getRefreshToken(String token)

    //public User getUserFromRefreshToken(RefreshToken refreshToken)

    public User getUserByID(Integer id)
    {
        System.out.println("The id is " + id);
        try {
            //User users = this.template.queryForObject(

            User user = this.template.queryForObject(
                    "SELECT id, email, user_status_id, salt, hashed_password " +
                            "FROM idm.user WHERE id=:id;",
                    new MapSqlParameterSource()
                            .addValue("id", id, Types.INTEGER),
                    (rs, rowNum) ->
                            new User()
                                    .setId(rs.getInt("id"))
                                    .setEmail(rs.getString("email"))
                                    .setUserStatus(UserStatus.fromId(rs.getInt("user_status_id")))
                                    .setSalt(rs.getString("salt"))
                                    .setHashedPassword(rs.getString("hashed_password"))

            );
            return user;

        } catch (DataAccessException e) {
            System.out.println("There error 2");
            throw new ResultError(IDMResults.USER_NOT_FOUND);

        }

    }




}
