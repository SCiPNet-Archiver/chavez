package com.cantomiletea.chavez.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class InitialUserInfo implements CommandLineRunner {

    private final UserInfoRepo userInfoRepo;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
//        UserInfoEntity manager = new UserInfoEntity();
//        manager.setUsername("Manager");
//        manager.setPassword(passwordEncoder.encode("password"));
//        manager.setEmail("manager@manager.com");
//
//        UserInfoEntity admin = new UserInfoEntity();
//        admin.setUsername("Admin");
//        admin.setPassword(passwordEncoder.encode("password"));
//        admin.setEmail("admin@admin.com");
//
//        UserInfoEntity user = new UserInfoEntity();
//        user.setUsername("User");
//        user.setPassword(passwordEncoder.encode("password"));
//        user.setEmail("user@user.com");
//
//        userInfoRepo.saveAll(List.of(manager, admin, user));
    }
}
