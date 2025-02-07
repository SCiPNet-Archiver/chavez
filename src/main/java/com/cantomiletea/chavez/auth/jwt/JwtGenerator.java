package com.cantomiletea.chavez.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtGenerator {

    private final JwtEncoder jwtEncoder;

    public String generateAccessToken(Authentication authentication) {

        log.info("[JwtGenerator:generateAccessToken] Token Creation Started for:{}", authentication.getName());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("scipnet")
                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plus(15 , ChronoUnit.MINUTES)) // Actually controls expiry time
                .expiresAt(Instant.now().plus(1 , ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("scope", "USER")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(Authentication authentication) {

        log.info("[JwtGenerator:generateRefreshToken] Token Creation Started for:{}", authentication.getName());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("scipnet")
                .issuedAt(Instant.now())
                // Matches amount in AuthService
                .expiresAt(Instant.now().plus(15 , ChronoUnit.DAYS))
                .subject(authentication.getName())
                .claim("scope", "REFRESH_TOKEN")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
