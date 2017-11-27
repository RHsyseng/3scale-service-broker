package com.redhat.syseng.openshift.service.broker.model.binding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BindingResult {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Credentials credentials;

    public BindingResult(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return "BindingResult{" +
                "credentials=" + credentials +
                '}';
    }

    public static class Credentials
    {
        private String url;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty("user_key")
        private String userKey;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String username;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String password;

        public Credentials(String url, String userKey) {
            this.url = url;
            this.userKey = userKey;
        }

        public Credentials(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUserKey() {
            return userKey;
        }

        public void setUserKey(String userKey) {
            this.userKey = userKey;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "Credentials{" +
                    "url='" + url + '\'' +
                    ", userKey='" + userKey + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }
}
