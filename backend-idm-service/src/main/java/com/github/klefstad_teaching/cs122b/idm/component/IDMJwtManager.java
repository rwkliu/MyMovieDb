package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.idm.config.IDMServiceConfig;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class IDMJwtManager
{
    private final JWTManager jwtManager;

    @Autowired
    public IDMJwtManager(IDMServiceConfig serviceConfig)
    {
        this.jwtManager =
            new JWTManager.Builder()
                .keyFileName(serviceConfig.keyFileName())
                .accessTokenExpire(serviceConfig.accessTokenExpire())
                .maxRefreshTokenLifeTime(serviceConfig.maxRefreshTokenLifeTime())
                .refreshTokenExpire(serviceConfig.refreshTokenExpire())
                .build();
    }

    private SignedJWT buildAndSignJWT(JWTClaimsSet claimsSet)
        throws JOSEException
    {

        JWSHeader header =
                new JWSHeader.Builder(JWTManager.JWS_ALGORITHM)
                        .keyID(jwtManager.getEcKey().getKeyID())
                        .type(JWTManager.JWS_TYPE)
                        .build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(jwtManager.getSigner());
        return signedJWT;
    }

    private void verifyJWT(SignedJWT jwt)
        throws JOSEException, BadJOSEException
    {

    }

    public String buildAccessToken(User user)
    {
        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getEmail())
                        .expirationTime(Date.from(Instant.now().plus(jwtManager.getAccessTokenExpire())))
                        .claim(JWTManager.CLAIM_ID, user.getId())    // we set claims like values in a map
                        .claim(JWTManager.CLAIM_ROLES, user.getRoles())
                        .issueTime(Date.from(Instant.now()))
                        .build();

        try {
            SignedJWT jwt = buildAndSignJWT(claimsSet);
            return jwt.serialize();

        }  catch (JOSEException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void verifyAccessToken(String jws)
    {
        try{
            SignedJWT signedJWT = SignedJWT.parse(jws);

            signedJWT.verify(jwtManager.getVerifier());
            jwtManager.getJwtProcessor().process(signedJWT, null);
            Instant expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
            if(Instant.now().isAfter(expirationTime)) {
                throw new ResultError(IDMResults.ACCESS_TOKEN_IS_EXPIRED);
            }
        }
        catch(IllegalStateException e){
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        } catch (BadJOSEException e) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        } catch (ParseException e) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        } catch (JOSEException e) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }



    }

    public RefreshToken buildRefreshToken(User user)
    {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenStatus(TokenStatus.ACTIVE);
        refreshToken.setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()));
        refreshToken.setMaxLifeTime(Instant.now().plus(jwtManager.getMaxRefreshTokenLifeTime()));

        return refreshToken;
    }

    public boolean hasExpired(RefreshToken refreshToken)
    {
        if(Instant.now().isAfter(refreshToken.getExpireTime())){
            return true;
        }
        if(Instant.now().isAfter(refreshToken.getMaxLifeTime())) {
            return true;
        }
        return false;
    }

    public boolean needsRefresh(RefreshToken refreshToken)
    {
        return false;
    }

    public void updateRefreshTokenExpireTime(RefreshToken refreshToken)
    {
        refreshToken.setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()));

        /*
        if(refreshToken.getMaxLifeTime().isAfter(Instant.now().plus(jwtManager.getRefreshTokenExpire()))){
            refreshToken.setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()));
            //update expire time in db
            //return same refreshtoken and new accessToken
        }
        else{
            //update old refreshToken status to Revoked in DB
            //return new refreshToken and new accessToken
        }
       */
        //refreshToken.setMaxLifeTime(Instant.now().plus(jwtManager.getMaxRefreshTokenLifeTime()));
    }

    private UUID generateUUID()
    {
        return UUID.randomUUID();
    }
}
