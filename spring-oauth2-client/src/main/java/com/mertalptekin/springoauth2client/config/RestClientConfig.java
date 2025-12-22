package com.mertalptekin.springoauth2client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

// Client uygulama üzerinden resource server istek atarken buradaki bean sayesinde Token bilgileri otomatik olarak
// Header  içerisine gömülür. Token bilgilerini okuyup header manuel olarak atmamıza gerek kalmaz.
// client resource server haberleşmesini görmüş olduk.

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        return RestClient.builder()
                .requestInterceptor(requestInterceptor)
                .build();
    }

}