package com.cantomiletea.chavez.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoMapper {

    private final PasswordEncoder passwordEncoder;
    public UserInfoEntity convertToEntity(UserRegistrationDto userRegistrationDto) {
        UserInfoEntity userInfoEntity = new UserInfoEntity();
        userInfoEntity.setUsername(userRegistrationDto.username());
        userInfoEntity.setEmail(userRegistrationDto.email());
        userInfoEntity.setPassword(passwordEncoder.encode(userRegistrationDto.password()));
        return userInfoEntity;
    }
}
