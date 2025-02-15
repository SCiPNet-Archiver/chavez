package com.cantomiletea.chavez.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepo extends JpaRepository<UserInfoEntity, Long> {
    Optional<UserInfoEntity> findByEmail(String email);
    Optional<UserInfoEntity> findByUsername(String username);
    Optional<UserInfoEntity> findByEmailOrUsername(String email, String username);
}
