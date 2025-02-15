package com.cantomiletea.chavez.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
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

    public String generateAccessToken(UserDetails user) {

        log.info("[JwtGenerator:generateAccessToken] Token Creation Started for:{}", user.getUsername());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("scipnet")
                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plus(15 , ChronoUnit.MINUTES)) // Actually controls expiry time
                .expiresAt(Instant.now().plus(1 , ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("scope", "USER")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(UserDetails user) {

        log.info("[JwtGenerator:generateRefreshToken] Token Creation Started for:{}", user.getUsername());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("scipnet")
                .issuedAt(Instant.now())
                // Matches amount in AuthService
                .expiresAt(Instant.now().plus(15 , ChronoUnit.DAYS))
                .subject(user.getUsername())
                .claim("scope", "REFRESH_TOKEN")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
