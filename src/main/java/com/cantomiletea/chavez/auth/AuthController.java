package com.cantomiletea.chavez.auth;

import com.cantomiletea.chavez.user.UserEditDto;
import com.cantomiletea.chavez.user.UserLoginDto;
import com.cantomiletea.chavez.user.UserRegistrationDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginDto userLoginDto,
                                              BindingResult bindingResult, HttpServletResponse res) {

        log.info("[AuthController:authenticateUser]Authentication Process Started for user:{}",userLoginDto.username());
        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            log.error("[AuthController:authenticateUser]Errors in user:{}",errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        return ResponseEntity.ok(authService.getAccessTokenFromCredentials(userLoginDto, res));
    }

    @PostMapping("/refresh-token")
    @PreAuthorize("hasAuthority('SCOPE_REFRESH_TOKEN')")
    public ResponseEntity<?> getAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(authService.getAccessTokenUsingRefreshToken(authorizationHeader));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto,
                                          BindingResult bindingResult, HttpServletResponse httpServletResponse){

        log.info("[AuthController:registerUser]Signup Process Started for user:{}",userRegistrationDto.username());
        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            log.error("[AuthController:registerUser]Errors in user:{}",errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        return ResponseEntity.ok(authService.registerUser(userRegistrationDto, httpServletResponse));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> deleteUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                        @PathVariable String username) {

        log.info("[AuthController:deleteUser]Deleting user:{}",username);
        authService.softDeleteUser(authorizationHeader, username);
        return ResponseEntity.ok().build();



    }

    @PutMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> editUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                      @Valid @RequestBody UserEditDto userEditDto,
                                      BindingResult bindingResult) {
        log.info("[AuthController:editUser]Editing user:{}", userEditDto);
        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            log.error("[AuthController:editUser]Errors in user:{}", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        authService.editUser(authorizationHeader, userEditDto);
        return ResponseEntity.ok().build();

    }
}
