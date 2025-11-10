package com.mertalptekin.springoauthserver.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.print.attribute.standard.Media;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. OAuth 2.0 Yetkilendirme Uç Noktaları için Güvenlik Filtresi
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()); // OpenID Connect'i etkinleştir

        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:5173")); // React app origin
            config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
            config.setAllowCredentials(true);
            config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
            return config;
        }));

        http
                // /oauth2/authorize gibi uç noktalara
                // kimlik doğrulanmamışsa, giriş sayfasına yönlendir
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // İstemci uygulamasından gelen token'ları doğrulamak için
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    // 2. Kullanıcı Kimlik Doğrulaması (Giriş Formu) için Güvenlik Filtresi
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated() // Tüm istekler kimlik doğrulaması gerektirir
                )
                .formLogin(Customizer.withDefaults()); // Varsayılan giriş formunu etkinleştir

        return http.build();
    }


    // 3. Test için hafızada bir kullanıcı oluştur
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    // 4. İstemci (client-mvc) uygulamasını tanımla
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client-mvc-oidc") // İstemci ID
                .clientSecret("{noop}secret") // İstemci şifresi // confidential client ise
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // En yaygın akış
                .redirectUri("http://localhost:9000/login/oauth2/code/client-mvc-oidc") // İstemcinin yönlendirme adresi
                .scope(OidcScopes.OPENID) // OIDC standardı
                .scope(OidcScopes.PROFILE) // Profil bilgilerini almak için
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build()) // Kullanıcı onayı iste
                .build();

        // reeact-spa istemcisi için kayıt (PKCE ile)
        RegisteredClient reactClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("react-oidc-client") // no secret public client
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:5173/login/oauth2/code/react-oidc-client")
                .postLogoutRedirectUri("http://localhost:5173/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("offline_access") // Refresh token için gerekli
                .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(5))   // access token süresi
                .refreshTokenTimeToLive(Duration.ofMinutes(7))      // refresh token süresi
                .reuseRefreshTokens(true)                        // refresh token tekrar kullanılabilir mi
                .build())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true) // <-- PKCE aktif
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient, reactClient);
    }

    // 5. Token'ları imzalamak için bir JWK kaynağı (anahtar çifti) oluştur
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    // 5a. RSA anahtar çifti oluşturmak için yardımcı metot
    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    // 6. JWT'leri çözmek (decode) için bir bean
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // 7. Yetkilendirme sunucusunun ayarları (örn: issuer URI)
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        // http://127.0.0.1:9000 olacağını varsayıyoruz
        return AuthorizationServerSettings.builder().build();
    }
}