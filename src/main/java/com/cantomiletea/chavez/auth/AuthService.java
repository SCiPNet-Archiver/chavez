package com.cantomiletea.chavez.auth;

import com.cantomiletea.chavez.auth.jwt.JwtGenerator;
import com.cantomiletea.chavez.auth.refresh.RefreshTokenEntity;
import com.cantomiletea.chavez.auth.refresh.RefreshTokenRepo;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoMapper;
import com.cantomiletea.chavez.user.UserInfoRepo;
import com.cantomiletea.chavez.user.UserRegistrationDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final String ERR_USER_ALREADY_EXISTS = "User already exists";

    private final UserInfoRepo userInfoRepo;
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserInfoMapper userInfoMapper;
    public AuthResponseDto getJwtTokensAfterAuthentication(Authentication authentication,
                                                           HttpServletResponse res) {

        try {
            var userInfoEntity = userInfoRepo.findByUsername(authentication.getName())
                    .orElseThrow(() -> {
                        log.error("[AuthService:userSignInAuth] User: {} not found",authentication.getName());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND");
                    });

            String accessToken = jwtGenerator.generateAccessToken(authentication);
            log.info("[AuthService:userSignInAuth] Access token for user: {}, has been generated",userInfoEntity.getUsername());

            String refreshToken = jwtGenerator.generateRefreshToken(authentication);
            log.info("[AuthService:userSignInAuth] Refresh token for user: {}, has been generated",userInfoEntity.getUsername());
            saveUserRefreshToken(userInfoEntity, refreshToken);
            createRefreshTokenCookie(res, refreshToken);

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
//                    .accessTokenExpiry(15 * 60)
                    .accessTokenExpiry(60)
                    .username(userInfoEntity.getUsername())
                    .tokenType(TokenType.Bearer)
                    .build();
        } catch (Exception e) {
            log.error("[AuthService:userSignInAuth] Error getting access token:",e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please try again");
        }
    }

    private Cookie createRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token",refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        // Matches amount in JwtGenerator
        refreshTokenCookie.setMaxAge(15 * 24 * 60 * 60 ); // in seconds
        response.addCookie(refreshTokenCookie);
        return refreshTokenCookie;
    }

    private void saveUserRefreshToken(UserInfoEntity userInfoEntity, String refreshToken) {
        var refreshTokenEntity = RefreshTokenEntity.builder()
                .user(userInfoEntity)
                .refreshToken(refreshToken)
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshTokenEntity);
    }
    public Object getAccessTokenUsingRefreshToken(String authorizationHeader) {

        if(!authorizationHeader.startsWith(TokenType.Bearer.name())){
            return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Please verify your token type");
        }

        final String refreshToken = authorizationHeader.substring(7);

        //Find refreshToken from database and should not be revoked : Same thing can be done through filter.
        var refreshTokenEntity = refreshTokenRepo.findByRefreshToken(refreshToken)
                .filter(tokens-> !tokens.isRevoked())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Refresh token revoked"));

        UserInfoEntity userInfoEntity = refreshTokenEntity.getUser();

        //Now create the Authentication object
        Authentication authentication =  createAuthenticationObject(userInfoEntity);

        //Use the authentication object to generate new accessToken as the Authentication object that we will have may not contain correct role.
        String accessToken = jwtGenerator.generateAccessToken(authentication);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
//                .accessTokenExpiry(15 * 60)
                .accessTokenExpiry(60)
                .username(userInfoEntity.getUsername())
                .tokenType(TokenType.Bearer)
                .build();
    }

    private static Authentication createAuthenticationObject(UserInfoEntity userInfoEntity) {
        // Extract user details from UserDetailsEntity
        String username = userInfoEntity.getUsername();
        String password = userInfoEntity.getPassword();

        return new UsernamePasswordAuthenticationToken(username, password);
    }

    public boolean userEmailExists(UserRegistrationDto dto) {
        return userInfoRepo.findByEmail(dto.email()).isPresent();
    }

    public boolean userUsernameExists(UserRegistrationDto dto) {
        return userInfoRepo.findByUsername(dto.username()).isPresent();
    }

    public AuthResponseDto registerUser(UserRegistrationDto userRegistrationDto, HttpServletResponse httpServletResponse){

        try{
            log.info("[AuthService:registerUser]User Registration Started with :::{}",userRegistrationDto);

            if (userEmailExists(userRegistrationDto) || userUsernameExists(userRegistrationDto)) {
                throw new Exception(ERR_USER_ALREADY_EXISTS);
            }

            //TODO: Eventually, we should be able to differentiate between an email already being registered
            //TODO: and a username already being registered. This would be good for responsiveness on the frontend

            UserInfoEntity userDetailsEntity = userInfoMapper.convertToEntity(userRegistrationDto);
            Authentication authentication = createAuthenticationObject(userDetailsEntity);


            // Generate a JWT token
            String accessToken = jwtGenerator.generateAccessToken(authentication);
            String refreshToken = jwtGenerator.generateRefreshToken(authentication);

            UserInfoEntity savedUserDetails = userInfoRepo.save(userDetailsEntity);
            saveUserRefreshToken(userDetailsEntity,refreshToken);

            createRefreshTokenCookie(httpServletResponse,refreshToken);

            log.info("[AuthService:registerUser] User:{} Successfully registered",savedUserDetails.getUsername());
            return   AuthResponseDto.builder()
                    .accessToken(accessToken)
//                    .accessTokenExpiry(15 * 60)
                    .accessTokenExpiry(60)
                    .username(savedUserDetails.getUsername())
                    .tokenType(TokenType.Bearer)
                    .build();


        }catch (Exception e){
            log.error("[AuthService:registerUser]Exception while registering the user due to: {}", e.getMessage());
            if (e.getMessage().equals(ERR_USER_ALREADY_EXISTS)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }

    }

}
