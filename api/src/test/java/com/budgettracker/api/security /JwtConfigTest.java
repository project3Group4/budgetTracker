package com.budgettracker.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtConfigTest {
    private static final String ISSUER = "http://localhost:8080";
    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef";

    // checks that a token signed with the app's key can be decoded with the correct issuer and subject.
    @Test
    void createsAndAcceptsTokenSignedWithAppKey() {
        JwtConfig config = new JwtConfig();
        SecretKey key = config.appJwtKey(toBase64(TEST_KEY));
        JwtEncoder encoder = config.jwtEncoder(key);
        JwtDecoder decoder = config.jwtDecoder(key, ISSUER);

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("test-user")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        String token = encoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(),
                                claims))
                .getTokenValue();

        // the decoder should accept a current token signed with the matching key.
        Jwt decodedToken = decoder.decode(token);

        assertEquals("test-user", decodedToken.getSubject());
        assertEquals(ISSUER, decodedToken.getIssuer().toString());
    }

    // checks that a token signed with a different key is rejected.
    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtConfig config = new JwtConfig();
        SecretKey signingKey = config.appJwtKey(toBase64(TEST_KEY));
        SecretKey differentKey = config.appJwtKey(
                toBase64("abcdef0123456789abcdef0123456789"));

        JwtEncoder encoder = config.jwtEncoder(signingKey);
        JwtDecoder decoder = config.jwtDecoder(differentKey, ISSUER);

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("test-user")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        String token = encoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(),
                                claims))
                .getTokenValue();

        // the decoder should reject a token signed with a key it does not have.
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    private static String toBase64(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }
}