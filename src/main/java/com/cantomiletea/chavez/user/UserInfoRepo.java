package com.cantomiletea.chavez.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepo extends JpaRepository<UserInfoEntity, Long> {
    Optional<UserInfoEntity> findByUsernameAndActiveTrue(String username);
    Optional<UserInfoEntity> findByEmailAndActiveTrue(String username);
    @Query("select u from UserInfoEntity u where u.active = true and (u.username = :username or u.email = :username)")
    Optional<UserInfoEntity> findByEmailOrUsernameAndActiveTrue(@Param("username") String username);
}

