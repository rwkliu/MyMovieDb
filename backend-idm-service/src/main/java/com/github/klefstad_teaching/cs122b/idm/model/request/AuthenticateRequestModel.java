package com.github.klefstad_teaching.cs122b.idm.model.request;

public class AuthenticateRequestModel {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
