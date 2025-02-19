package com.cantomiletea.chavez.auth;

import com.cantomiletea.chavez.auth.exception.EmailAlreadyRegisteredException;
import com.cantomiletea.chavez.auth.exception.UsernameTakenException;
import com.cantomiletea.chavez.auth.jwt.JwtGenerator;
import com.cantomiletea.chavez.auth.jwt.JwtUtils;
import com.cantomiletea.chavez.auth.refresh.RefreshTokenEntity;
import com.cantomiletea.chavez.auth.refresh.RefreshTokenRepo;
import com.cantomiletea.chavez.user.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserInfoDetailsService userInfoDetailsService;
    private final JwtGenerator jwtGenerator;
    private final UserInfoRepo userInfoRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserInfoMapper userInfoMapper;
    private final JwtDecoder jwtDecoder;
    private final JwtUtils jwtUtils;
    private final UserInfoService userInfoService;

    private UserDetails authenticateUser(UserLoginDto userLoginDto) {
        try {
            UserDetails userDetails = userInfoDetailsService.loadUserByUsername(userLoginDto.username());

            if (!passwordEncoder.matches(userLoginDto.password(), userDetails.getPassword())) {
                throw new UsernameNotFoundException(userLoginDto.username());
            }

            return userDetails;

        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with given credentials");
        }
    }

    private AuthResponseDto makeAccessTokenResponse(UserDetails userDetails) {
        String accessToken = jwtGenerator.generateAccessToken(userDetails);

        return   AuthResponseDto.builder()
                .accessToken(accessToken)
//                    .accessTokenExpiry(15 * 60)
                .accessTokenExpiry(60)
                .username(userDetails.getUsername())
                .tokenType(TokenType.Bearer)
                .build();
    }

    private boolean userUsernameExists(UserRegistrationDto userRegistrationDto) {
        return userInfoRepo.findByUsernameAndActiveTrue(userRegistrationDto.username()).isPresent();
    }

    private boolean userEmailExists(UserRegistrationDto userRegistrationDto) {
        return userInfoRepo.findByEmailAndActiveTrue(userRegistrationDto.username()).isPresent();
    }

    public AuthResponseDto registerUser(UserRegistrationDto userRegistrationDto,
                                         HttpServletResponse httpServletResponse) {


        try {

            log.info("[AuthService:registerUser]User Registration Started with :::{}", userRegistrationDto);

            if (userUsernameExists(userRegistrationDto)) {
                throw new UsernameTakenException("Username is already taken");
            }

            if (userEmailExists(userRegistrationDto)) {
                throw new EmailAlreadyRegisteredException("Email is already registered");
            }

            UserInfoEntity userInfoEntity = userInfoMapper.convertToEntity(userRegistrationDto);
            UserInfoEntity savedUserDetails = userInfoRepo.save(userInfoEntity);

            UserDetails userDetails = userInfoDetailsService.loadUserByUsername(savedUserDetails.getUsername());


            String refreshToken = createAndSaveRefreshToken(userDetails);

            addRefreshTokenCookieToResponse(refreshToken, httpServletResponse);

            log.info("[AuthService:registerUser] User:{} Successfully registered", savedUserDetails.getUsername());
            return makeAccessTokenResponse(userDetails);

        } catch (ConstraintViolationException e) {
            log.error("[AuthService:registerUser] ERROR :: Password too simple");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password too simple");

        } catch (UsernameTakenException e) {
            log.error("[AuthService:registerUser] ERROR :: Username is taken");
            throw e;

        } catch (EmailAlreadyRegisteredException e) {
            log.error("[AuthService:registerUser] ERROR :: Email is already registered");
            throw e;

        } catch (Exception e){
            log.error("[AuthService:registerUser] ERROR :: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please try again");
        }
    }

    private void addRefreshTokenCookieToResponse(String refreshToken,
                                                 HttpServletResponse response) {

        Cookie cookie = new Cookie("refreshToken", refreshToken);

        // Makes cookie inaccessible to client-side (java)scripts.
        // Helps mitigate certain attacks, like cross-site scripting (XSS)
        cookie.setHttpOnly(true);

        // Makes it so the cookie is only sent to the client if it's being
        // requested over a secure HTTPS connection.
        // Set to true in prod
        cookie.setSecure(false);

        // Cookie will last 15 days. Note that the refresh token is also set
        // to last 15 days; make sure they match.
        cookie.setMaxAge(15 * 24 * 60 * 60);

        cookie.setDomain("localhost");
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    private String createAndSaveRefreshToken(UserDetails userDetails) {
        String refreshToken = jwtGenerator.generateRefreshToken(userDetails);

        try {
            UserInfoEntity userInfoEntity = userInfoRepo
                    .findByEmailOrUsernameAndActiveTrue(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername()));

            var refreshTokenEntity = RefreshTokenEntity.builder()
                    .user(userInfoEntity)
                    .refreshToken(refreshToken)
                    .revoked(false)
                    .build();
            refreshTokenRepo.save(refreshTokenEntity);

            return refreshToken;
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with given credentials");
        }
    }

    public AuthResponseDto getAccessTokenFromCredentials(UserLoginDto userLoginDto,
                                                         HttpServletResponse response) {

        UserDetails userDetails = authenticateUser(userLoginDto);
        String refreshToken = createAndSaveRefreshToken(userDetails);
        addRefreshTokenCookieToResponse(refreshToken, response);
        return makeAccessTokenResponse(userDetails);
    }

    public AuthResponseDto getAccessTokenUsingRefreshToken(String authHeader) {

        if (!authHeader.startsWith("Bearer")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please check token type");
        }

        String refreshToken = authHeader.substring(7); // remove "Bearer " from the header

        var refreshTokenEntity = refreshTokenRepo.findByRefreshToken(refreshToken)
                .filter(token -> !token.isRevoked())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is revoked"));

        UserInfoEntity user = refreshTokenEntity.getUser();
        UserDetails userDetails = userInfoDetailsService.loadUserByUsername(user.getUsername());

        return makeAccessTokenResponse(userDetails);
    }

    public void softDeleteUser(String authHeader, String username) {
        if (!authHeader.startsWith("Bearer")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please check token type");
        }

        try {
            // Get the user associated with the username
            UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            String accessToken = authHeader.substring(7); // remove "Bearer " from the header
            Jwt jwt = jwtDecoder.decode(accessToken);

            if (!jwt.getSubject().equals(user.getUsername())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or email does not match access token");
            }

            userInfoRepo.findByEmailOrUsernameAndActiveTrue(username)
                    .ifPresent(u -> {
                        u.setActive(false);
                        userInfoRepo.save(u);
                    });
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User "+ username +" not found");
        }
    }


    public void editUser(String authHeader, UserEditDto userEditDto) {

        if (!authHeader.startsWith("Bearer")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please check token type");
        }

        try {

            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);

            user.setUsername(userEditDto.username());
            user.setEmail(userEditDto.email());
            user.setBio(userEditDto.bio());
            user.setPfpUrl(userEditDto.pfpUrl());

            userInfoRepo.save(user);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username not found");
        }
    }
}