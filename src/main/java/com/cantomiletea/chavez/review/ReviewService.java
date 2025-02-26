package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.*;
import com.cantomiletea.chavez.scpapi.crom.CromClient;
import com.cantomiletea.chavez.scpapi.raw.RawScpDocClient;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoRepo;
import com.cantomiletea.chavez.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

import static com.cantomiletea.chavez.review.dto.ObjectClass.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewRepo reviewRepo;
    private final UserInfoService userInfoService;
    private final UserInfoRepo userInfoRepo;
    private final CromClient cromClient;
    private final RawScpDocClient rawScpDocClient;

    public void addReview(String authHeader, ReviewMinimalDto reviewMinimalDto) {

        try {
            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewMinimalDto.username().equals(user.getUsername()) &&
                !reviewMinimalDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity reviewEntity = reviewMapper.convertToEntity(reviewMinimalDto);
            reviewRepo.save(reviewEntity);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Article '"+ reviewMinimalDto.slug()+"' already reviewed by this user");
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

    public void editReview(String authHeader, ReviewMinimalDto reviewMinimalDto) {

        try {

            UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);
            if (!reviewMinimalDto.username().equals(user.getUsername()) &&
                    !reviewMinimalDto.username().equals(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ReviewEntity review = reviewRepo.findByUserAndSlug(user, reviewMinimalDto.slug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User has not reviewed article with slug '" + reviewMinimalDto.slug() + "'"));

            review.setRating(reviewMinimalDto.rating());
            review.setComment(reviewMinimalDto.comment());
            reviewRepo.save(review);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JWT username "+e.getMessage()+" not found");
        }
    }

    public List<ReviewResponseDto> getUserReviews(String username) {
        try {
            UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Map the reviews to the info of the article as well
            return reviewRepo.findAllByUser(user)
                    .stream().map(r -> {
                        var articleInfo = getReviewArticleInfo(r.getSlug());
                        return new ReviewResponseDto(
                                articleInfo.slug(),
                                articleInfo.title(),
                                articleInfo.altTitle(),
                                articleInfo.author(),
                                r.getRating(),
                                r.getComment()
                        );
                    }).toList();

        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username "+e.getMessage()+" not found");
        }

    }

    public List<ReviewArticleDto> getUserReviewsArticles(String username) {

        try {
            UserInfoEntity user = userInfoRepo.findByEmailOrUsernameAndActiveTrue(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Get the reviewed article slugs
            List<String> articleSlugs = reviewRepo.findAllByUser(user)
                    .stream().map(ReviewEntity::getSlug).toList();

            // Get the article data
            return getReviewArticles(articleSlugs);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username "+e.getMessage()+" not found");
        }

    }

    private ObjectClass getObjClassInTags(List<String> tags) {
        for (ObjectClass objClass : ObjectClass.values()) {
            if (tags.contains(objClass.name().toLowerCase())) {
                return objClass;
            }
        }
        return ESOTERIC;
    }

    private ReviewArticleInfoDto getReviewArticleInfo(String slug) {
        String baseQuery = """
                    query {
                        page(url: "http://scp-wiki.wikidot.com/%s") {
                            alternateTitles {
                                title
                            }
                            wikidotInfo {
                                title
                                createdBy {
                                  name
                                }
                                tags
                            }
                        }
                    }""";

        String query = baseQuery.formatted(slug);
        CromArticleResponseDto res = cromClient.executeQuery(query, CromArticleResponseDto.class)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Get title and author
        String title = res.getData().getPage().getWikidotInfo().getTitle();

        String altTitle = null;
        try {
            altTitle = res.getData().getPage().getAlternateTitles().getFirst().getTitle();
        } catch (NoSuchElementException ignored) { }

        String author = res.getData().getPage().getWikidotInfo().getCreatedBy().getName();

        ReviewArticleInfoDto reviewArticleInfoDto = new ReviewArticleInfoDto(
                slug, title, altTitle, author
        );

        log.info("[ReviewService::getReviewArticles] Got the article of {}", slug);

        return reviewArticleInfoDto;

    }

    private List<ReviewArticleDto> getReviewArticles(List<String> slugs) {
        List<ReviewArticleDto> reviewArticleDtos = new ArrayList<>();


        String baseQuery = """
                    query {
                        page(url: "http://scp-wiki.wikidot.com/%s") {
                            alternateTitles {
                                type
                                title
                            }
                            wikidotInfo {
                                title
                                createdBy {
                                  name
                                }
                                tags
                            }
                        }
                    }""";
        for (String slug : slugs) {
            String query = baseQuery.formatted(slug);
            CromArticleResponseDto res = cromClient.executeQuery(query, CromArticleResponseDto.class)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            // Get title and author
            String title = res.getData().getPage().getWikidotInfo().getTitle();
            String author = res.getData().getPage().getWikidotInfo().getCreatedBy().getName();
            ScpInfoDto scpInfo = null;

            // Get tags, check if "scp" is one of them
            var tags = res.getData().getPage().getWikidotInfo().getTags();

            if (tags.contains("scp")) {
                // Get object class
                ObjectClass objClass = getObjClassInTags(tags);

                // Get ACS, if applicable
                ScpAcsDto acs;

                try {
                    acs = rawScpDocClient.getScpAcsDto(slug);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                scpInfo = new ScpInfoDto(
                        res.getData().getPage().getWikidotInfo().getTitle(),
                        objClass.name().toLowerCase(),
                        acs
                );
            }

            ReviewArticleDto reviewArticleDto = new ReviewArticleDto(
                    title,
                    author,
                    scpInfo
            );

            reviewArticleDtos.add(reviewArticleDto);

            log.info("[ReviewService::getReviewArticles] Got the article of {}", slug);
        }

        return reviewArticleDtos;

    }


}
