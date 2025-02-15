package com.cantomiletea.chavez.auth;

import com.cantomiletea.chavez.auth.exception.EmailAlreadyRegisteredException;
import com.cantomiletea.chavez.auth.exception.UsernameTakenException;
import com.cantomiletea.chavez.auth.jwt.JwtGenerator;
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

    public boolean userEmailExists(UserRegistrationDto dto) {
        return userInfoRepo.findByEmail(dto.email()).isPresent();
    }

    public boolean userUsernameExists(UserRegistrationDto dto) {
        return userInfoRepo.findByUsername(dto.username()).isPresent();
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

        UserInfoEntity userInfoEntity = userInfoRepo.findByEmail(userDetails.getUsername()).orElse(
                userInfoRepo.findByUsername(userDetails.getUsername()).get());

        var refreshTokenEntity = RefreshTokenEntity.builder()
                .user(userInfoEntity)
                .refreshToken(refreshToken)
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshTokenEntity);

        return refreshToken;
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
}