package com.cantomiletea.chavez.user;

import com.cantomiletea.chavez.auth.jwt.JwtUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final JwtDecoder jwtDecoder;
    private final JwtUtils jwtUtils;
    private final UserInfoRepo userInfoRepo;

    public UserInfoEntity getUserInfoByJwt(@NotNull final String authHeader) throws UsernameNotFoundException {

        String jwtString = authHeader.substring(7);
        Jwt jwt = jwtDecoder.decode(jwtString);
        String username = jwtUtils.getUsername(jwt);

        return userInfoRepo.findByEmailOrUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
