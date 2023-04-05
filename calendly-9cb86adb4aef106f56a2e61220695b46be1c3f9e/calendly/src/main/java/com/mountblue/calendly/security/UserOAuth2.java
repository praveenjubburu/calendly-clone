package com.mountblue.calendly.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;



public class UserOAuth2 implements OAuth2User {

    private OAuth2User auth2User;


    public UserOAuth2(OAuth2User auth2User) {
        this.auth2User = auth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return auth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return auth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return auth2User.getAttribute("name");
    }


    public String getMail() {
        return auth2User.getAttribute("email");
    }

    public String getPhoneNumber() {
        return auth2User.getAttribute("phone number");
    }
}
