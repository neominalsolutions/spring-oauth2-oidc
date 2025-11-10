package com.mertalptekin.springoauth2client.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/user-info")
    public Map<String, Object> user(
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication,
            @RegisteredOAuth2AuthorizedClient("demo-client") OAuth2AuthorizedClient client) {

        OAuth2AccessToken token = client.getAccessToken();

        return Map.of(
                "authenticated", authentication != null,
                "userName", authentication != null ? authentication.getName() : null,
                "accessToken", token != null ? token.getTokenValue() : null,
                "scopes", token != null ? token.getScopes() : null
        );
    }
}