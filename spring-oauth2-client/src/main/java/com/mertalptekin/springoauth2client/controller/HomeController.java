package com.mertalptekin.springoauth2client.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Controller
public class HomeController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestClient restClient;

    public HomeController(OAuth2AuthorizedClientService authorizedClientService, RestClient restClient) {
        this.authorizedClientService = authorizedClientService;
        this.restClient = restClient;
    }

    // OAuth2AuthorizedClientService -> AuthServer tanımlı authenticated olan clientlara erişimi sağlayan
    // authenticated client bilgilerini okumamızı sağlayan servis.
    // OAuth2AuthenticationToken -> Oturum açan kullanıcı Access Token bilgilerine erişmek için kullanılan servis.

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OidcUser principal, OAuth2AuthenticationToken authentication) {
        if (principal != null) {
            // Token'dan gelen "claim"leri modele ekle
            model.addAttribute("username", principal.getClaim("sub")); // Genellikle kullanıcı adı
            model.addAttribute("claims", principal.getClaims());

            // Access Token

            // Access Token
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client != null && client.getAccessToken() != null) {
                String accessToken = client.getAccessToken().getTokenValue();
                model.addAttribute("accessToken", accessToken);
            }


            //import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
            String messages = this.restClient.get()
                    .uri("http://localhost:9090/api")
                    .attributes(clientRegistrationId("client-mvc-oidc"))
                    .retrieve()
                    .body(String.class);

            System.out.println("resource server message" + messages);




        }
        return "index"; // index.html'i render et
    }


}

