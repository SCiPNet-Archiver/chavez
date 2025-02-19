package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final UserInfoRepo userInfoRepo;

    public ReviewEntity convertToEntity(ReviewAddDto reviewAddDto) {
        ReviewEntity reviewEntity = new ReviewEntity();

        UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(reviewAddDto.username())
                        .orElseThrow(() -> new UsernameNotFoundException(reviewAddDto.username()));

        reviewEntity.setUser(user);
        reviewEntity.setSlug(reviewAddDto.slug());
        reviewEntity.setRating(reviewAddDto.rating());
        reviewEntity.setComment(reviewAddDto.comment());
        return reviewEntity;
    }
}
