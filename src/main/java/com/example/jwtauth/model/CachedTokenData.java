package com.example.jwtauth.model;

import java.io.Serializable;
import java.time.Instant;

public class CachedTokenData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String userId;

    private Instant createdAt;

    private Instant expiresAt;

    public CachedTokenData(){

    }

    public CachedTokenData(String token, String userId, Instant createdAt, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // implement builder pattern here
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String userId;
        private Instant createdAt;
        private Instant expiresAt;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public CachedTokenData build() {
            return new CachedTokenData(this.token, this.userId, this.createdAt, this.expiresAt);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }


    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isAboutToExpire(long bufferMillis) {
        return Instant.now().plusSeconds(bufferMillis).isAfter(expiresAt);
    }

    public long getRemainingTimeInSeconds() {
        return Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }


}
