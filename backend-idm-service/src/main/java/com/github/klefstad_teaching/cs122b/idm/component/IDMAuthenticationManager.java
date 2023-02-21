package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.repo.IDMRepo;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class IDMAuthenticationManager
{
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String       HASH_FUNCTION = "PBKDF2WithHmacSHA512";

    private static final int ITERATIONS     = 10000;
    private static final int KEY_BIT_LENGTH = 512;

    private static final int SALT_BYTE_LENGTH = 4;

    public final IDMRepo repo;

    @Autowired
    public IDMAuthenticationManager(IDMRepo repo)
    {
        this.repo = repo;
    }

    private static byte[] hashPassword(final char[] password, String salt)
    {
        return hashPassword(password, Base64.getDecoder().decode(salt));
    }

    private static byte[] hashPassword(final char[] password, final byte[] salt)
    {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_FUNCTION);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BIT_LENGTH);

            SecretKey key = skf.generateSecret(spec);

            return key.getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] genSalt()
    {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public User selectAndAuthenticateUser(String email, char[] password)
    {
        User user = repo.getUser(email);
        String prev_salt = user.getSalt();
        byte[] hashed_password = hashPassword(password, prev_salt);
        String base64Encoded_hashedPassword = Base64.getEncoder().encodeToString(hashed_password);

        if(user.getHashedPassword().equals(base64Encoded_hashedPassword)){
            return user;
        }
        else{
            throw new ResultError(IDMResults.INVALID_CREDENTIALS);
        }

        //return null;
    }

    public void createAndInsertUser(String email, char[] password)
    {
        //hash password
        byte[] my_salt = genSalt();
        byte[] hashed_password = hashPassword(password, my_salt);
        String base64Encoded_hashedPassword = Base64.getEncoder().encodeToString(hashed_password);
        String base64Encoded_hashedSalt = Base64.getEncoder().encodeToString(my_salt);
        //create User
        User user = new User();
        user.setHashedPassword(base64Encoded_hashedPassword);
        user.setEmail(email);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setSalt(base64Encoded_hashedSalt);

        repo.insertUser(user);

    }

    public void insertRefreshToken(RefreshToken refreshToken)
    {
        repo.insertRefreshToken(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token)
    {

        return this.repo.getRefreshToken(token);
    }

    public void updateRefreshTokenExpireTime(RefreshToken token)
    {
        repo.updateRefreshTokenExpireTime(token);

    }

    public void expireRefreshToken(RefreshToken token)
    {
        repo.updateRefreshTokenStatusExpire(token);
    }

    public void revokeRefreshToken(RefreshToken token)
    {
        repo.updateRefreshTokenStatusRevoke(token);
    }

    public User getUserFromRefreshToken(RefreshToken refreshToken)
    {
        return repo.getUserByID(refreshToken.getUserId());
        //return null;
    }
}
