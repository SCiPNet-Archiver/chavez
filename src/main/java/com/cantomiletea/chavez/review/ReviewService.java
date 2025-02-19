package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.ReviewDto;
import com.cantomiletea.chavez.review.dto.ReviewDeleteDto;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewRepo reviewRepo;
    private final UserInfoService userInfoService;

    public void addReview(String authHeader, ReviewDto reviewDto) {

        try {
            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewDto.username().equals(user.getUsername()) &&
                !reviewDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity reviewEntity = reviewMapper.convertToEntity(reviewDto);
            reviewRepo.save(reviewEntity);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Article '"+ reviewDto.slug()+"' already reviewed by this user");
        }
    }

    public void deleteReview(String authHeader, ReviewDeleteDto reviewDeleteDto) {

        try {

            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewDeleteDto.username().equals(user.getUsername()) &&
                    !reviewDeleteDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity review = reviewRepo.findByUserAndSlug(user, reviewDeleteDto.slug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User has not reviewed article with slug '" + reviewDeleteDto.slug() + "'"));

            reviewRepo.delete(review);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        }
    }

    public void editReview(String authHeader, ReviewDto reviewDto) {

        try {

            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewDto.username().equals(user.getUsername()) &&
                    !reviewDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity review = reviewRepo.findByUserAndSlug(user, reviewDto.slug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User has not reviewed article with slug '" + reviewDto.slug() + "'"));

            review.setRating(reviewDto.rating());
            review.setComment(reviewDto.comment());
            reviewRepo.save(review);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        }
    }
}
