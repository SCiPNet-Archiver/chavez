package com.cantomiletea.chavez.auth.jwt;

import com.cantomiletea.chavez.auth.RSAKeyRecord;
import com.cantomiletea.chavez.auth.UserInfoDetails;
import com.cantomiletea.chavez.user.UserInfoRepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
@Slf4j
public class JwtUtils {

    @Getter
    private final JwtDecoder jwtDecoder;

    public JwtUtils(RSAKeyRecord rsaKeyRecord, UserInfoRepo userInfoRepo) {
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKeyRecord.rsaPublicKey()).build();
        this.userInfoRepo = userInfoRepo;
    }

    public String getUsername(Jwt jwtToken){
        return jwtToken.getSubject();
    }

    public boolean isTokenValid(Jwt jwtToken, UserDetails userDetails){
        final String username = getUsername(jwtToken);
        boolean isTokenExpired = getIfTokenIsExpired(jwtToken);
        boolean isTokenUserSameAsDatabase = username.equals(userDetails.getUsername());
        return !isTokenExpired && isTokenUserSameAsDatabase;

    }

    private boolean getIfTokenIsExpired(Jwt jwtToken) {
        return Objects.requireNonNull(jwtToken.getExpiresAt()).isBefore(Instant.now());
    }

    private final UserInfoRepo userInfoRepo;
    public UserDetails userDetails(String username){
        return userInfoRepo
                .findByUsernameAndActiveTrue(username)
                .map(UserInfoDetails::new)
                // Catch this if we want to handle the error
                .orElseThrow(()-> new UsernameNotFoundException("User "+username+" not found"));
    }
}
