package com.cantomiletea.chavez.pending;

import com.cantomiletea.chavez.user.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingRepo extends JpaRepository<PendingEntity, Long> {
    List<PendingEntity> findAllByUser(UserInfoEntity userInfo);
    Optional<PendingEntity> findByUserAndSlug(UserInfoEntity userInfoEntity, String slug);
}
