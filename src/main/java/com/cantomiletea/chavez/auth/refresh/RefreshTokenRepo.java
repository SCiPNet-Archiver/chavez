package com.cantomiletea.chavez.auth.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByRefreshToken(String token);

    @Query(value = "SELECT rt.* FROM refresh_token rt " +
            "INNER JOIN user_detail ud ON rt.user_id = ud.id " +
            "WHERE ud.username = :username AND rt.revoked = false ", nativeQuery = true)
    Optional<RefreshTokenEntity> findAllRefreshTokenByUsername(String username);
}
