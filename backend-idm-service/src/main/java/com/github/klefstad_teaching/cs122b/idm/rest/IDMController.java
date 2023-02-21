package com.github.klefstad_teaching.cs122b.idm.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BasicResults;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.component.IDMAuthenticationManager;
import com.github.klefstad_teaching.cs122b.idm.component.IDMJwtManager;
import com.github.klefstad_teaching.cs122b.idm.model.request.AuthenticateRequestModel;
import com.github.klefstad_teaching.cs122b.idm.model.request.LoginRequestModel;
import com.github.klefstad_teaching.cs122b.idm.model.request.RefreshRequestModel;
import com.github.klefstad_teaching.cs122b.idm.model.request.RegisterRequestModel;
import com.github.klefstad_teaching.cs122b.idm.model.response.AuthenticateResponseModel;
import com.github.klefstad_teaching.cs122b.idm.model.response.LoginResponseModel;
import com.github.klefstad_teaching.cs122b.idm.model.response.RefreshResponseModel;
import com.github.klefstad_teaching.cs122b.idm.model.response.RegisterResponseModel;
import com.github.klefstad_teaching.cs122b.idm.repo.IDMRepo;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.util.Validate;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.text.ParseException;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
public class IDMController
{
    private final IDMAuthenticationManager authManager;
    private final IDMJwtManager            jwtManager;
    private final Validate                 validate;
    public void validateEmailandPassword(String email, char[] password){
        Pattern validEmailPattern = Pattern.compile(("^[A-Za-z0-9+_.-]+@(.+)$"));
        if(!validEmailPattern.matcher(email).matches()){
            throw new ResultError(IDMResults.EMAIL_ADDRESS_HAS_INVALID_FORMAT);
        }
        if(email.length()>32 || email.length()<6){
            throw new ResultError(IDMResults.EMAIL_ADDRESS_HAS_INVALID_LENGTH);
        }
        //at least one uppercase alpha, one lowercase alpha, and one numeric
        Pattern validPasswordPattern = Pattern.compile("^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9]).*$");
        if(!validPasswordPattern.matcher(String.valueOf(password)).matches()){
            throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_CHARACTER_REQUIREMENT);
        }
        if(password.length>20 || password.length<10){
            throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_LENGTH_REQUIREMENTS);
        }
    }

    @Autowired
    public IDMController(IDMAuthenticationManager authManager,
                         IDMJwtManager jwtManager,
                         Validate validate)
    {
        this.authManager = authManager;
        this.jwtManager = jwtManager;
        this.validate = validate;
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseModel> register(
            @RequestBody RegisterRequestModel request
            )
    {
        //validate email
        validateEmailandPassword(request.getEmail(), request.getPassword());
        //register error if already exists
        //need to register the user
        authManager.createAndInsertUser(request.getEmail(), request.getPassword());
        RegisterResponseModel response = new RegisterResponseModel();
        response.setResult(IDMResults.USER_REGISTERED_SUCCESSFULLY);
        return response.toResponse();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseModel> login(
            @RequestBody LoginRequestModel request
            )
    {
        validateEmailandPassword(request.getEmail(), request.getPassword());

       // authManager.createAndInsertUser(request.getEmail(), request.getPassword());

        LoginResponseModel response = new LoginResponseModel();
        User user = authManager.selectAndAuthenticateUser(request.getEmail(), request.getPassword());
        if(user.getUserStatus().id()==2){
            throw new ResultError(IDMResults.USER_IS_LOCKED);
        }
        if(user.getUserStatus().id()==3){
            throw new ResultError(IDMResults.USER_IS_BANNED);
        }
        response.setResult(IDMResults.USER_LOGGED_IN_SUCCESSFULLY);
        response.setAccessToken(jwtManager.buildAccessToken(user));
        RefreshToken rt = jwtManager.buildRefreshToken(user);

        response.setRefreshToken(rt.getToken());
        //check this passes if commented out
        //System.out.println(rt.getToken());
        authManager.insertRefreshToken(rt);
        return response.toResponse();

    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseModel> refresh(
            @RequestBody RefreshRequestModel request
    )
    {
        System.out.println("POst refresh is here ");
        RefreshResponseModel response = new RefreshResponseModel();
        //request.getRefreshToken()
        String  s = request.getRefreshToken();
        //validate the length
        if(s.length()!=36){
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_LENGTH);
        }
        try {
            UUID u = UUID.fromString(s);

        } catch(Exception e){
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_FORMAT);
        }
        RefreshToken rt = authManager.verifyRefreshToken(request.getRefreshToken());
        if(rt.getTokenStatus().id()==2){
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        }
        if(rt.getTokenStatus().id()==3){
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_REVOKED);
        }
        if(jwtManager.hasExpired(rt)){
            //update the refresh token status to expired in db
            authManager.expireRefreshToken(rt);
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        }
        //last check - update refreshToken expire time
        //RefreshToken oldRefreshToken = rt;
        User user = authManager.getUserFromRefreshToken(rt);
        //updates the expire time
        jwtManager.updateRefreshTokenExpireTime(rt);

        if(rt.getExpireTime().isAfter(rt.getMaxLifeTime())){
            //update old refreshtoken status to revoked in db
            authManager.revokeRefreshToken(rt);

            //return new refreshtoken and new accesstoken
            response.setAccessToken(jwtManager.buildAccessToken(user));
            response.setRefreshToken(jwtManager.buildRefreshToken(user).getToken());//check

        }
        else{
            //update same refresh token expireTime in DB
            authManager.updateRefreshTokenExpireTime(rt);
            //return same refreshtoken and new accesstoken
            response.setAccessToken(jwtManager.buildAccessToken(user));
            response.setRefreshToken(s);
        }


        response.setResult(IDMResults.RENEWED_FROM_REFRESH_TOKEN);
        return response.toResponse();

    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponseModel> refresh(
            @RequestBody AuthenticateRequestModel request
    )
    {
        AuthenticateResponseModel response = new AuthenticateResponseModel();

        //1.Verifying that the token is valid and issued by us by calling jwt.verify(jwtManager.getVerifier())
        //2.Checking that the claims are consistent with what we expect by calling jwtManager.getJwtProcessor().process(jwt, null)
       // 3.Manually checking that the expireTime of the token has not passed.
        //check if accesstoken is valid
        jwtManager.verifyAccessToken(request.getAccessToken());


        response.setResult(IDMResults.ACCESS_TOKEN_IS_VALID);

        return response.toResponse();
    }


}
