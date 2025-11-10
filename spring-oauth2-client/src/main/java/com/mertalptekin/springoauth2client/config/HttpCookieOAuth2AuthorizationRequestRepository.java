package com.mertalptekin.springoauth2client.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import java.util.Base64;


// HttpCookieOAuth2AuthorizationRequestRepository, OAuth2 yetkilendirme isteklerini (authorization requests) HTTP çerezlerinde (cookies) saklamak ve geri yüklemek için kullanılan bir sınıftır. OAuth2 tabanlı kimlik doğrulama süreçlerinde, kullanıcıyı yetkilendirme sunucusuna yönlendirmeden önce oluşturulan yetkilendirme isteği bilgilerini geçici olarak saklamak gerekir. Bu sınıf, bu bilgileri güvenli bir şekilde çerezlerde tutar ve gerektiğinde geri alır.

// HttpCookieOAuth2AuthorizationRequestRepository sınıfının amacı OAuth2 login sürecinde kullanılan authorization request bilgisini cookie’de saklamak ve login tamamlandıktan sonra temizlemektir.

/* Akış ->
 1. Login başlatılırken (/oauth2/authorize isteği):
 saveAuthorizationRequest metodu çağrılır.
 OAuth2AuthorizationRequest nesnesi serialize edilip oauth2_auth_request adlı cookie’ye yazılır.
 2. Authorization server’dan redirect geldikten sonra:
 Spring Security, redirect URL’i ile gelen kodu alır.
 removeAuthorizationRequest metodu çağrılır ve cookie silinir (deleteCookie).

 Not: Bu cookie sadece login akışı için geçici bir depolama alanıdır.
 Bu cookie’yi access token veya refresh token saklamak için kullanamayız; bu bilgiler Spring Security’nin SecurityContext’inde tutulur
*/

public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // Çerezlerin geçerlilik süresi (saniye cinsinden)
    // Bu geçerlilik süresini kısa tutarsak güvenliği artırabiliriz, çünkü yetkilendirme istekleri genellikle kısa ömürlüdür.
    public static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie != null) {
            return deserialize(cookie.getValue());
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        String serialized = serialize(authorizationRequest);
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    // --- Helper Methods ---
    private String serialize(OAuth2AuthorizationRequest obj) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(Base64.getUrlDecoder().decode(value));
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) return cookie;
        }
        return null;
    }
}
