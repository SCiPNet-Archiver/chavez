package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.ReviewDto;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final UserInfoRepo userInfoRepo;

    public ReviewEntity convertToEntity(ReviewDto reviewDto) {
        ReviewEntity reviewEntity = new ReviewEntity();

        UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(reviewDto.username())
                        .orElseThrow(() -> new UsernameNotFoundException(reviewDto.username()));

        reviewEntity.setUser(user);
        reviewEntity.setSlug(reviewDto.slug());
        reviewEntity.setRating(reviewDto.rating());
        reviewEntity.setComment(reviewDto.comment());
        return reviewEntity;
    }
}
