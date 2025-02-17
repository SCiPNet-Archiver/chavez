package com.cantomiletea.chavez.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepo extends JpaRepository<UserInfoEntity, Long> {
    Optional<UserInfoEntity> findByUsernameAndActiveTrue(String username);
    Optional<UserInfoEntity> findByEmailAndActiveTrue(String username);
    Optional<UserInfoEntity> findByEmailOrUsernameAndActiveTrue(String email, String username);
}

