package com.budgettracker.api.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    private static final MacAlgorithm APP_JWT_ALGORITHM = MacAlgorithm.HS256;
    private static final String HMAC_KEY_ALGORITHM = "HmacSHA256";

    /**
     * turns the private key from the environment into a form java can use.
     */
    @Bean
    SecretKey appJwtKey(
            @Value("${app.jwt.secret-base64}") String secretBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
        return new SecretKeySpec(keyBytes, HMAC_KEY_ALGORITHM);
    }

    /**
     * checks that tokens came from this api and have not expired.
     */
    @Bean
    JwtDecoder jwtDecoder(
            SecretKey appJwtKey,
            @Value("${app.jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(appJwtKey)
                .macAlgorithm(APP_JWT_ALGORITHM)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    /**
     * gives the api a way to sign the tokens it creates for users.
     */
    @Bean
    JwtEncoder jwtEncoder(SecretKey appJwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(appJwtKey));
    }
}