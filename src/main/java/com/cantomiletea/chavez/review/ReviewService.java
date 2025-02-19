package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
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

    public void addReview(String authHeader, ReviewAddDto reviewAddDto) {

        try {
            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewAddDto.username().equals(user.getUsername()) &&
                !reviewAddDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity reviewEntity = reviewMapper.convertToEntity(reviewAddDto);
            reviewRepo.save(reviewEntity);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Article '"+ reviewAddDto.slug()+"' already reviewed by this user");
        }
    }
}
