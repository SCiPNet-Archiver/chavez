package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.ReviewMinimalDto;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final UserInfoRepo userInfoRepo;

    public ReviewEntity convertToEntity(ReviewMinimalDto reviewMinimalDto) {
        ReviewEntity reviewEntity = new ReviewEntity();

        UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(reviewMinimalDto.username())
                        .orElseThrow(() -> new UsernameNotFoundException(reviewMinimalDto.username()));

        reviewEntity.setUser(user);
        reviewEntity.setSlug(reviewMinimalDto.slug());
        reviewEntity.setRating(reviewMinimalDto.rating());
        reviewEntity.setComment(reviewMinimalDto.comment());
        return reviewEntity;
    }
}
