package com.aayush.authforge.authfordgeapi.auth.oauth;

import java.util.Map;

public class GoogleOAuthUserInfo extends OAuthUserInfo {

    public GoogleOAuthUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

    @Override
    public String getImage() {
        return attributes.get("picture").toString();
    }
}
