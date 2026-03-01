package com.aayush.authforge.authfordgeapi.auth.oauth;

import java.util.Map;

public abstract class OAuthUserInfo {

    protected Map<String, Object> attributes;

    public OAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getEmail();
    public abstract String getName();
    public abstract String getImage();
}
