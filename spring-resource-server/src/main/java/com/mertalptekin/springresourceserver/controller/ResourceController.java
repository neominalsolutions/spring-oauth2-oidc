package com.mertalptekin.springresourceserver.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {

    // @AuthenticationPrincipal
    // @AuthenticationPrincipal, Spring Security'de kimlik doğrulama bilgilerine erişmek
    // için kullanılan bir anotasyondur. Bu anotasyon, bir denetleyici metodunda,
    // kimlik doğrulama nesnesini (örneğin, Jwt, UserDetails, vb.) doğrudan
    // parametre olarak almanızı sağlar.


    @GetMapping
    public Map<String, String> hello(@AuthenticationPrincipal Jwt jwt) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + jwt.getClaimAsString("sub"));
        response.put("token", jwt.getTokenValue());
        return response;
    }
}
