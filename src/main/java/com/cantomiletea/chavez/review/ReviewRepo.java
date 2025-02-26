package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.user.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByUserAndSlug(UserInfoEntity user, String slug);
    List<ReviewEntity> findAllByUser(UserInfoEntity user);
}
